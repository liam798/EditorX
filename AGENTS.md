# EditorX 开发者与 AI 代理协作指南

本文档为参与 EditorX 项目开发的开发者和 AI 代码代理（Agents）提供技术规范与协作约定。

## 项目概览

- **语言/运行时**：Kotlin 2.1.x，JVM Toolchain 21
- **构建工具**：Gradle（Kotlin DSL）
- **主要模块**：
  - `core`：核心 API 与插件运行时（包根：`editorx.*`）
  - `gui`：桌面应用 GUI 实现（入口：`editorx.gui.GuiAppKt`）
  - `plugins/*`：功能插件模块

## 核心架构原则

### 1. 模块边界

- **`core` 模块**：不应依赖 `gui` 模块的具体实现
- **`gui` 模块**：通过 `GuiExtension` 接口与插件交互
- **插件模块**：只依赖 `core` 模块的 API

### 2. 包命名规范

- **顶级包名**：统一使用 `editorx`
- **插件包名**：必须以 `editorx.plugins.*` 开头（源码插件）
- **核心 API**：`editorx.core.plugin.*`、`editorx.core.gui.*`、`editorx.core.service.*`
- **GUI 实现**：`editorx.gui.*`

### 3. 插件系统

#### 插件类型与来源

- **SOURCE 插件**：通过 `SourcePluginLoader` 从 ClassPath 加载，使用 `ServiceLoader<Plugin>` 发现
- **JAR 插件**：通过 `JarPluginLoader` 从 `plugins/` 目录加载，使用独立 `URLClassLoader`
- **组合加载**：`DuplexPluginLoader` 同时加载两种类型的插件

#### 插件标识

- 插件以 `PluginInfo.id` 作为唯一标识（全局唯一且稳定）
- 重复 ID 会被拒绝加载
- 插件生命周期状态：`LOADED` → `STARTED` → `STOPPED` / `FAILED`

#### 插件上下文（PluginContext）

`PluginContext` 是类（非接口），提供：
- `gui(): GuiExtension?` - 获取 GUI 扩展接口
- `registerService(serviceClass, instance)` - 注册服务
- `unregisterService(serviceClass, instance)` - 取消注册服务
- `pluginId()`, `pluginInfo()` - 获取插件信息

#### GUI 扩展（GuiExtension）

`GuiExtension` 接口位于 `editorx.core.gui` 包，提供：
- 文件类型注册（`registerFileType`, `unregisterAllFileTypes`）
- 语法高亮注册（`registerSyntaxHighlighter`, `unregisterAllSyntaxHighlighters`）
- 格式化器注册（`registerFormatter`, `unregisterAllFormatters`）
- 文件处理器注册（`registerFileHandler`, `unregisterAllFileHandlers`）
- 工具栏按钮管理（`addToolBarItem`, `setToolBarItemEnabled`）
- 工作区和文件操作（`openWorkspace`, `openFile`）

**重要**：插件在 `deactivate()` 时必须调用对应的 `unregisterAll*()` 方法清理资源。

### 4. 服务注册机制

- **ServiceRegistry**：核心服务注册表，支持多实例注册
- **服务注册**：插件通过 `PluginContext.registerService()` 注册服务
- **服务查找**：通过 `ServiceRegistry.getService(serviceClass)` 获取所有注册的服务实例
- **示例服务**：`BuildService`（构建能力）、`DecompilerService`（反编译能力）

### 5. 关键数据结构

- **LoadedPlugin**：加载后的插件数据（包含 `plugin`, `origin`, `path`, `classLoader`, `closeable`）
- **PluginSnapshot**：插件快照信息（用于 UI 展示，包含 `info`, `origin`, `state`, `path`, `disabled`）
- **PluginOrigin**：插件来源枚举（`SOURCE`, `JAR`）

## 代码规范

### 命名约定

- 类名使用 PascalCase
- 函数和变量使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 包名全部小写，单词间无分隔符

### 模块依赖规则

```
core (不依赖 gui)
  ↑
  ├── gui (依赖 core)
  └── plugins/* (依赖 core)
```

### 插件开发规范

1. **插件类实现**：实现 `editorx.core.plugin.Plugin` 接口
2. **服务声明**：在 `META-INF/services/editorx.core.plugin.Plugin` 中声明实现类
3. **资源清理**：在 `deactivate()` 中调用 `GuiExtension` 的 `unregisterAll*()` 方法
4. **服务注册**：在 `activate()` 中注册服务，系统会在 `deactivate()` 时自动取消注册

### 文件组织

```
core/src/main/kotlin/editorx/core/
  ├── plugin/              # 插件 API
  │   ├── loader/         # 插件加载器
  │   ├── Plugin.kt
  │   ├── PluginContext.kt
  │   ├── PluginManager.kt
  │   └── ...
  ├── service/            # 服务注册表
  ├── gui/                # GUI 扩展接口
  └── ...

gui/src/main/kotlin/editorx/gui/
  ├── core/               # GUI 核心实现
  │   ├── GuiExtensionImpl.kt
  │   └── ...
  └── ...
```

## 常见任务指南

### 添加新的插件加载器

如果需要在现有 `SourcePluginLoader` 和 `JarPluginLoader` 基础上添加新的加载方式：

1. 创建新的加载器类，实现 `PluginLoader` 接口
2. 在 `DuplexPluginLoader` 中集成新加载器（或创建新的组合加载器）
3. 更新 `GuiApp` 使用新的组合加载器

### 添加新的服务类型

1. 在 `core/src/main/kotlin/editorx/core/service/` 中定义服务接口
2. 插件实现该接口并注册：`context.registerService(ServiceClass::class.java, instance)`
3. 其他组件通过 `ServiceRegistry.getService(ServiceClass::class.java)` 获取服务

### 添加新的 GUI 扩展能力

1. 在 `editorx.core.gui.GuiExtension` 接口中添加新方法
2. 在 `editorx.gui.core.GuiExtensionImpl` 中实现该方法
3. 插件通过 `context.gui()?.newMethod()` 使用新能力

## 对 AI 代理的特殊要求

### 代码修改规则

1. **包名检查**：所有新增代码必须使用 `editorx` 顶级包
2. **模块边界**：确保不违反模块依赖规则（`core` 不依赖 `gui`）
3. **接口 vs 实现**：
   - `PluginContext` 是类，不是接口
   - `GuiExtension` 是接口，位于 `editorx.core.gui` 包
   - `GuiExtensionImpl` 是实现类，位于 `editorx.gui.core` 包
4. **命名一致性**：
   - 使用 `LoadedPlugin`（不是 `DiscoveredPlugin`）
   - 使用 `PluginSnapshot`（不是 `PluginRecord`）
   - 使用 `PluginOrigin.SOURCE`（不是 `CLASSPATH`）
   - 使用 `GuiExtension`（不是 `PluginGuiProvider`）

### 代码审查检查清单

修改代码时，请确保：

- [ ] 包名符合规范（`editorx.*`）
- [ ] 模块依赖方向正确（`core` ← `gui` ← `plugins/*`）
- [ ] 插件在 `deactivate()` 中正确清理资源
- [ ] 服务注册和取消注册配对
- [ ] 类型名称使用最新命名（`LoadedPlugin`, `PluginSnapshot`, `GuiExtension` 等）
- [ ] 编译通过（`./gradlew build`）
- [ ] 无 lint 错误

### 测试建议

虽然项目当前可能缺少自动化测试，但建议：

1. 手动验证插件加载、激活、停用流程
2. 验证资源清理（卸载插件后资源是否释放）
3. 验证服务注册和查找功能
4. 验证跨模块调用不会导致循环依赖

## 参考资源

- [架构文档](docs/ARCHITECTURE.md) - 详细的架构设计说明
- [README](README.md) - 项目概览和使用指南
- Kotlin 官方编码规范
- Gradle Kotlin DSL 文档

---

**最后更新**：请保持本文档与代码实现同步。如有架构变更，请及时更新本文档。
