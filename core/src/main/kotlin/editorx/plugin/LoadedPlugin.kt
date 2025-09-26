package editorx.plugin

import java.net.URLClassLoader

/**
 * 已加载的插件包装器
 * 包含插件实例、元信息和相关的类加载器信息
 */
data class LoadedPlugin(
    val plugin: Plugin,
    val classLoader: URLClassLoader? = null
) {

    /** 插件ID */
    val id: String get() = plugin.getInfo().id

    /** 插件名称 */
    val name: String get() = plugin.getInfo().name

    /** 插件版本 */
    val version: String get() = plugin.getInfo().version
}
