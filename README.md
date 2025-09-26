# EditorX

EditorX 是一个基于 Kotlin/JVM 的可扩展桌面编辑器，采用模块化与插件化架构，内置命令面板、活动栏、侧边栏以及插件系统（源码插件与 JAR 插件）。

## 构建与运行

- 启动 GUI：`./gradlew :gui:run`
- 构建所有模块：`./gradlew build`

运行后会自动扫描并加载：
- 源码插件：类路径下包前缀 `editorx` 实现 `editorx.plugin.Plugin` 的类
- JAR 插件：放入运行目录 `plugins/`，自动扫描实现 `Plugin` 接口的类

## 主要特性

### 核心架构
- **模块化设计**：`core`、`gui`、`plugins` 模块分离
- **插件系统**：支持源码插件和JAR插件，自动类发现
- **事件驱动**：基于事件总线的松耦合架构
- **命令系统**：统一的命令注册和执行机制

### 用户界面
- **ActivityBar**：左侧活动栏，包含Explorer等工具
- **SideBar**：侧边栏，显示活动栏选中的内容
- **Editor**：主编辑器区域，支持多标签页
- **StatusBar**：底部状态栏，显示文件信息
- **TitleBar**：顶部菜单栏

### 插件系统
- **自动类发现**：JAR插件自动扫描实现`Plugin`接口的类
- **统一接口**：所有插件通过`getInfo()`方法提供元信息
- **生命周期管理**：插件的激活、禁用和卸载
- **资源隔离**：每个插件有独立的类加载器和资源路径

### 编辑器功能
- **多标签页**：支持多个文件同时编辑
- **文件操作**：打开、保存、另存为
- **拖拽支持**：拖拽文件到编辑器打开
- **状态显示**：显示当前文件的行列信息

## 模块结构

```
EditorX
├── core/                    # 核心模块
│   ├── plugin/             # 插件API
│   ├── command/            # 命令系统
│   ├── event/              # 事件总线
│   ├── settings/           # 设置管理
│   └── workspace/          # 工作区管理
├── gui/                    # GUI模块
│   ├── main/               # 主窗口和组件
│   │   ├── activitybar/    # 活动栏
│   │   ├── sidebar/        # 侧边栏
│   │   ├── editor/         # 编辑器
│   │   ├── titlebar/       # 标题栏
│   │   └── statusbar/      # 状态栏
│   ├── ui/                 # UI组件
│   └── services/           # GUI服务
└── plugins/                # 插件模块
    └── testplugin/         # 测试插件
```

## 插件开发

### 源码插件
```kotlin
class MyPlugin : Plugin {
    override fun getInfo(): PluginInfo {
        return PluginInfo(
            id = "my-plugin",
            name = "My Plugin",
            version = "1.0.0"
        )
    }
    
    override fun activate(context: PluginContext) {
        // 插件激活逻辑
    }
    
    override fun deactivate() {
        // 插件禁用逻辑
    }
}
```

### JAR插件
1. 实现`Plugin`接口
2. 打包为JAR文件
3. 放入`plugins/`目录
4. 系统自动扫描并加载

## 技术栈

- **语言**：Kotlin 2.1.x
- **运行时**：JVM 21
- **构建工具**：Gradle (Kotlin DSL)
- **UI框架**：Swing
- **主题**：FlatLaf + Material3

## 开发规范

更多协作规范见 `AGENTS.md`。

## 许可证

本项目采用开源许可证，具体信息请查看LICENSE文件。