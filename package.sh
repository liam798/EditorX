#!/bin/bash

# EditorX 打包脚本
# 用于构建和打包 EditorX 应用程序

set -e  # 遇到错误立即退出

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 输出信息函数
info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

# 检查必要工具
check_requirements() {
    info "检查构建环境..."
    
    if ! command -v java &> /dev/null; then
        error "Java 未安装，请安装 Java 21 或更高版本"
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        warn "Java 版本可能过低，建议使用 Java 21"
    fi
    
    if [ ! -f "./gradlew" ]; then
        error "未找到 gradlew 脚本"
    fi
    
    chmod +x ./gradlew
}

# 清理之前的构建
clean_build() {
    info "清理之前的构建..."
    ./gradlew clean
}

# 构建项目
build_project() {
    info "构建项目..."
    ./gradlew build -x test
}

# 创建分发包
create_distribution() {
    info "创建分发包..."
    
    # 先创建标准的 installDist
    ./gradlew :gui:installDist
    
    # 自动从 settings.gradle.kts 中读取插件模块列表
    info "从 settings.gradle.kts 检测插件模块..."
    local plugin_jars=()
    
    # 解析 settings.gradle.kts 文件，提取所有 :plugins:xxx 模块
    while IFS= read -r line; do
        # 匹配 include(":plugins:xxx") 格式
        if [[ "$line" =~ include\(\":plugins:([^\"]+)\"\) ]]; then
            local module_name="${BASH_REMATCH[1]}"
            plugin_jars+=("${module_name}.jar")
            info "  ✓ 检测到插件模块: $module_name -> ${module_name}.jar"
        fi
    done < settings.gradle.kts
    
    if [ ${#plugin_jars[@]} -eq 0 ]; then
        error "未能从 settings.gradle.kts 检测到任何插件模块"
    fi
    
    info "共检测到 ${#plugin_jars[@]} 个插件模块"
    
    local install_dir="gui/build/install/gui"
    
    if [ ! -d "$install_dir" ]; then
        error "安装目录不存在: $install_dir"
    fi
    
    # 创建 plugins 目录
    local plugins_dir="$install_dir/plugins"
    mkdir -p "$plugins_dir"
    
    # 移动插件 JAR 到 plugins 目录
    local lib_dir="$install_dir/lib"
    if [ ! -d "$lib_dir" ]; then
        error "lib 目录不存在: $lib_dir"
    fi
    
    info "移动插件 JAR 到 plugins 目录..."
    for plugin_jar in "${plugin_jars[@]}"; do
        local source_file="$lib_dir/$plugin_jar"
        if [ -f "$source_file" ]; then
            mv "$source_file" "$plugins_dir/"
            info "  ✓ $plugin_jar"
        else
            warn "  未找到插件: $plugin_jar"
        fi
    done
    
    # 验证插件 JAR 已从 lib 目录移除
    info "验证插件 JAR 移动结果..."
    local remaining_plugins=0
    for plugin_jar in "${plugin_jars[@]}"; do
        if [ -f "$lib_dir/$plugin_jar" ]; then
            warn "  警告: $plugin_jar 仍在 lib 目录中"
            remaining_plugins=$((remaining_plugins + 1))
        fi
    done
    
    if [ $remaining_plugins -eq 0 ]; then
        info "  ✓ 所有插件 JAR 已移动到 plugins 目录"
    else
        error "部分插件 JAR 移动失败，请检查"
    fi
    
    # 删除旧的 zip 和 tar 文件（如果存在）
    local zip_file="gui/build/distributions/gui.zip"
    local tar_file="gui/build/distributions/gui.tar"
    if [ -f "$zip_file" ]; then
        rm -f "$zip_file"
        info "已删除旧的 zip 文件"
    fi
    if [ -f "$tar_file" ]; then
        rm -f "$tar_file"
        info "已删除旧的 tar 文件"
    fi
    
    # 创建 zip 包
    info "创建 ZIP 包..."
    local zip_dir="$(dirname "$zip_file")"
    mkdir -p "$zip_dir"
    
    # 获取当前工作目录（脚本所在目录）
    local current_dir="$(pwd)"
    local abs_zip_file="${current_dir}/${zip_file}"
    
    # 进入安装目录的父目录，以便 zip 中包含正确的目录结构（gui/ 作为根）
    cd "$(dirname "$install_dir")"
    zip -r "$abs_zip_file" "$(basename "$install_dir")" > /dev/null 2>&1
    local zip_exit_code=$?
    cd "$current_dir"
    
    if [ $zip_exit_code -ne 0 ]; then
        error "ZIP 包创建失败"
    fi
    
    # 验证 zip 文件
    if [ ! -f "$zip_file" ]; then
        error "ZIP 包创建失败: $zip_file"
    fi
    
    # 验证 zip 包中 lib 目录不包含插件 JAR
    info "验证 ZIP 包内容..."
    local zip_plugin_in_lib=$(unzip -l "$zip_file" 2>/dev/null | grep -E "gui/lib/(android|git|json|yaml|xml|smali|i18n-(zh|en))\.jar" | wc -l | tr -d ' ')
    if [ "$zip_plugin_in_lib" -gt 0 ]; then
        warn "警告: ZIP 包中 lib 目录仍包含 $zip_plugin_in_lib 个插件 JAR"
        info "ZIP 包 lib 目录内容："
        unzip -l "$zip_file" 2>/dev/null | grep "gui/lib/.*\.jar" | sed 's/^/    /'
    else
        info "  ✓ ZIP 包中 lib 目录不包含插件 JAR"
    fi
    
    info "分发包创建成功："
    info "  - $zip_file"
}

# 生成 macOS .app（可选）
create_macos_app() {
    # 仅在 macOS 上执行
    if [[ "$(uname -s)" != "Darwin" ]]; then
        warn "当前系统非 macOS，跳过 .app 生成"
        return 0
    fi

    info "生成 macOS .app（jpackage）..."

    if ! command -v jpackage &> /dev/null; then
        error "未找到 jpackage，请安装包含 jpackage 的 JDK（或将 jpackage 加入 PATH）"
    fi

    local install_dir="gui/build/install/gui"
    if [ ! -d "$install_dir" ]; then
        error "安装目录不存在: $install_dir"
    fi

    local jpackage_dir="gui/build/jpackage"
    local input_dir="$jpackage_dir/input"
    local iconset_dir="$jpackage_dir/EditorX.iconset"
    local icns_file="$jpackage_dir/EditorX.icns"
    local out_dir="gui/build/distributions"

    rm -rf "$jpackage_dir"
    mkdir -p "$input_dir"

    # 拷贝应用依赖（lib/*.jar -> input/）
    cp "$install_dir/lib/"*.jar "$input_dir/"

    # 拷贝插件（plugins/*.jar -> input/plugins/）
    if [ -d "$install_dir/plugins" ]; then
        mkdir -p "$input_dir/plugins"
        cp "$install_dir/plugins/"*.jar "$input_dir/plugins/" 2>/dev/null || true
    fi

    # 生成 icns 图标
    local icon_src="gui/src/main/resources/icon.png"
    if [ ! -f "$icon_src" ]; then
        warn "未找到 $icon_src，跳过 --icon（.app 将使用默认图标）"
        icns_file=""
    else
        rm -rf "$iconset_dir"
        mkdir -p "$iconset_dir"

        # iconutil 需要固定命名（icon_16x16.png 等）
        # 说明：源图当前为 500x500，会按需缩放生成各尺寸。
        sips -z 16 16   "$icon_src" --out "$iconset_dir/icon_16x16.png"       >/dev/null 2>&1
        sips -z 32 32   "$icon_src" --out "$iconset_dir/icon_16x16@2x.png"    >/dev/null 2>&1
        sips -z 32 32   "$icon_src" --out "$iconset_dir/icon_32x32.png"       >/dev/null 2>&1
        sips -z 64 64   "$icon_src" --out "$iconset_dir/icon_32x32@2x.png"    >/dev/null 2>&1
        sips -z 128 128 "$icon_src" --out "$iconset_dir/icon_128x128.png"     >/dev/null 2>&1
        sips -z 256 256 "$icon_src" --out "$iconset_dir/icon_128x128@2x.png"  >/dev/null 2>&1
        sips -z 256 256 "$icon_src" --out "$iconset_dir/icon_256x256.png"     >/dev/null 2>&1
        sips -z 512 512 "$icon_src" --out "$iconset_dir/icon_256x256@2x.png"  >/dev/null 2>&1
        sips -z 512 512 "$icon_src" --out "$iconset_dir/icon_512x512.png"     >/dev/null 2>&1
        sips -z 1024 1024 "$icon_src" --out "$iconset_dir/icon_512x512@2x.png" >/dev/null 2>&1

        if command -v iconutil &> /dev/null; then
            iconutil -c icns "$iconset_dir" -o "$icns_file" >/dev/null 2>&1 || true
        else
            warn "未找到 iconutil，跳过生成 .icns（.app 将使用默认图标）"
            icns_file=""
        fi
    fi

    # 使用当前 java.home 作为 runtime-image（确保能运行当前编译产物）
    local runtime_home
    runtime_home=$(java -XshowSettings:properties -version 2>&1 | awk -F' = ' '/java.home =/ {print $2; exit}' | tr -d '\r')
    if [ -z "$runtime_home" ] || [ ! -x "$runtime_home/bin/java" ]; then
        error "无法解析 java.home 或找到 $runtime_home/bin/java；请确保 PATH 中的 java 可用"
    fi

    # 清理旧的 .app
    rm -rf "$out_dir/EditorX.app"

    # 生成 app-image（.app）
    local jpackage_args=(
        --type app-image
        --name "EditorX"
        --dest "$out_dir"
        --input "$input_dir"
        --main-jar "gui.jar"
        --main-class "editorx.gui.GuiAppKt"
        --runtime-image "$runtime_home"
    )
    if [ -n "$VERSION" ]; then
        jpackage_args+=( --app-version "$VERSION" )
    fi
    if [ -n "$icns_file" ] && [ -f "$icns_file" ]; then
        jpackage_args+=( --icon "$icns_file" )
    fi

    jpackage "${jpackage_args[@]}"

    if [ ! -d "$out_dir/EditorX.app" ]; then
        error ".app 生成失败：$out_dir/EditorX.app"
    fi

    # 生成可分发的 zip（macOS 推荐用 ditto 保留资源 fork）
    local app_zip="$out_dir/EditorX-macOS.app.zip"
    rm -f "$app_zip"
    if command -v ditto &> /dev/null; then
        (cd "$out_dir" && ditto -c -k --sequesterRsrc --keepParent "EditorX.app" "$(basename "$app_zip")")
    else
        warn "未找到 ditto，回退到 zip -r 打包 .app（可能丢失部分元数据）"
        (cd "$out_dir" && zip -r "$(basename "$app_zip")" "EditorX.app" > /dev/null 2>&1)
    fi

    info "macOS 产物已生成："
    info "  - $out_dir/EditorX.app"
    info "  - $app_zip"
}

# 创建发布目录
create_release_dir() {
    local version=${1:-"dev"}
    local release_dir="release/editorx-${version}"
    
    info "创建发布目录: $release_dir"
    mkdir -p "$release_dir"
    
    # 复制分发包（zip）
    cp gui/build/distributions/gui.zip "$release_dir/"
    # 若存在 macOS .app zip，也一并复制（可选）
    if [ -f "gui/build/distributions/EditorX-macOS.app.zip" ]; then
        cp "gui/build/distributions/EditorX-macOS.app.zip" "$release_dir/"
    fi
    
    # 获取版本信息（如果可能）
    if command -v git &> /dev/null && git rev-parse --git-dir > /dev/null 2>&1; then
        local git_version=$(git describe --tags --always 2>/dev/null || git rev-parse --short HEAD)
        echo "$git_version" > "$release_dir/VERSION"
    fi
    
    # 创建构建信息
    cat > "$release_dir/BUILD_INFO" << EOF
Build Date: $(date -u +"%Y-%m-%d %H:%M:%S UTC")
Build User: $(whoami)
Java Version: $(java -version 2>&1 | head -n 1)
Gradle Version: $(./gradlew --version 2>&1 | grep "Gradle" | head -n 1)
EOF
    
    info "发布文件已创建到: $release_dir"
}

# 显示帮助信息
show_help() {
    cat << EOF
EditorX 打包脚本

用法: $0 [选项]

选项:
    -v, --version VERSION    指定版本号（用于发布目录命名）
    -c, --clean               清理构建（默认会清理）
    -b, --build-only          仅构建，不创建分发包
    --mac-app                 额外生成 macOS .app（需要 macOS + jpackage）
    -h, --help                显示此帮助信息

示例:
    $0                        # 标准打包
    $0 -v 1.0.0              # 指定版本号打包
    $0 -b                    # 仅构建，不创建分发包
    $0 --mac-app             # 标准打包 + 生成 macOS .app
    $0 --clean               # 清理并打包

EOF
}

# 解析命令行参数
VERSION=""
CLEAN=true
BUILD_ONLY=false
MAC_APP=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--version)
            VERSION="$2"
            shift 2
            ;;
        -c|--clean)
            CLEAN=true
            shift
            ;;
        -b|--build-only)
            BUILD_ONLY=true
            shift
            ;;
        --mac-app)
            MAC_APP=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            error "未知选项: $1"
            ;;
    esac
done

# 主流程
main() {
    info "开始打包 EditorX..."
    
    check_requirements
    
    if [ "$CLEAN" = true ]; then
        clean_build
    fi
    
    build_project
    
    if [ "$BUILD_ONLY" = false ]; then
        create_distribution

        if [ "$MAC_APP" = true ]; then
            create_macos_app
        fi
        
        if [ -n "$VERSION" ]; then
            create_release_dir "$VERSION"
        fi
    fi
    
    info "打包完成！"
}

# 执行主流程
main
