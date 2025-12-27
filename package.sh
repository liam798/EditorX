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

# 创建发布目录
create_release_dir() {
    local version=${1:-"dev"}
    local release_dir="release/editorx-${version}"
    
    info "创建发布目录: $release_dir"
    mkdir -p "$release_dir"
    
    # 复制分发包（只复制 zip）
    cp gui/build/distributions/gui.zip "$release_dir/"
    
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
    -h, --help                显示此帮助信息

示例:
    $0                        # 标准打包
    $0 -v 1.0.0              # 指定版本号打包
    $0 -b                    # 仅构建，不创建分发包
    $0 --clean               # 清理并打包

EOF
}

# 解析命令行参数
VERSION=""
CLEAN=true
BUILD_ONLY=false

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
        
        if [ -n "$VERSION" ]; then
            create_release_dir "$VERSION"
        fi
    fi
    
    info "打包完成！"
}

# 执行主流程
main

