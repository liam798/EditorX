package editorx.plugin

import editorx.event.EventBus
import editorx.event.PluginLoaded
import editorx.event.PluginUnloaded
import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * 插件管理器
 * 负责插件的加载、卸载和生命周期管理
 */
class PluginManager(
    private val contextFactory: PluginContextFactory,
    private val eventBus: EventBus? = null
) {
    private val logger = Logger.getLogger(PluginManager::class.java.name)
    private val loadedPlugins = ConcurrentHashMap<String, LoadedPlugin>()

    fun loadPlugins() {
        logger.info("开始加载插件...")

        val pluginsJars = scanPluginJars()
        logger.info("找到 ${pluginsJars.size} 个JAR插件文件")
        pluginsJars.forEach { jarFile -> loadJarPlugin(jarFile) }

        logger.info("插件加载完成，共加载 ${loadedPlugins.size} 个插件")
    }

    fun listLoaded(): List<LoadedPlugin> = loadedPlugins.values.toList()

    /**
     * 扫描插件JAR文件
     */
    private fun scanPluginJars(): Array<File> {
        val pluginDir = File("plugins")
        if (!pluginDir.exists()) {
            pluginDir.mkdirs()
            logger.info("插件目录不存在，已创建: ${pluginDir.absolutePath}")
        } else {
            return pluginDir.listFiles { file -> file.isFile && file.extension.lowercase() == "jar" } ?: arrayOf()
        }
        return arrayOf()
    }

    /**
     * 在JAR文件中查找实现Plugin接口的类
     */
    private fun findPluginMainClassInJar(classLoader: URLClassLoader, jarFile: File): Class<*>? {
        val pluginClasses = mutableListOf<Class<*>>()

        try {
            val jarFileObj = java.util.jar.JarFile(jarFile)
            val entries = jarFileObj.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.endsWith(".class") && !entry.name.contains("$")) {
                    val className = entry.name.replace('/', '.').replace(".class", "")

                    try {
                        val clazz = classLoader.loadClass(className)
                        if (Plugin::class.java.isAssignableFrom(clazz) && !clazz.isInterface) {
                            pluginClasses.add(clazz)
                            logger.info("找到Plugin实现类: $className")
                        }
                    } catch (e: Exception) {
                        // 忽略无法加载的类
                        logger.fine("无法加载类 $className: ${e.message}")
                    }
                }
            }
            jarFileObj.close()
        } catch (e: Exception) {
            logger.warning("扫描JAR文件 ${jarFile.name} 时出错: ${e.message}")
        }

        return pluginClasses.firstOrNull()
    }

    /**
     * 加载JAR格式的插件
     */
    private fun loadJarPlugin(jarFile: File) {
        try {
            logger.info("正在加载插件: ${jarFile.name}")

            // 为插件创建独立类加载器，设置父加载器为应用类加载器
            val classLoader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), this::class.java.classLoader)

            // 直接扫描JAR中的Plugin实现类
            val pluginMainClass = findPluginMainClassInJar(classLoader, jarFile)
            if (pluginMainClass == null) {
                logger.warning("插件 ${jarFile.name} 中未找到实现Plugin接口的类")
                return
            }
            logger.info("找到插件主类: ${pluginMainClass.name}")

            val plugin = pluginMainClass.getDeclaredConstructor().newInstance() as Plugin

            val loadedPlugin = LoadedPlugin(
                plugin = plugin,
                classLoader = classLoader
            )

            val context = contextFactory.createPluginContext(loadedPlugin)

            plugin.activate(context)
            loadedPlugins[loadedPlugin.name] = loadedPlugin
            eventBus?.publish(PluginLoaded(loadedPlugin.id, loadedPlugin.name, loadedPlugin.version))

            logger.info("插件加载成功: ${loadedPlugin.name} v${loadedPlugin.version}")
        } catch (e: Exception) {
            logger.severe("加载插件失败: ${jarFile.name}, 错误: ${e.message}")
            e.printStackTrace()
        }
    }

    fun unloadPlugin(pluginName: String) {
        loadedPlugins[pluginName]?.let { loadedPlugin ->
            try {
                runCatching { loadedPlugin.plugin.deactivate() }
                loadedPlugin.classLoader?.close()
                loadedPlugins.remove(pluginName)
                eventBus?.publish(PluginUnloaded(loadedPlugin.id, pluginName))
                logger.info("插件卸载成功: $pluginName")
            } catch (e: Exception) {
                logger.severe("卸载插件失败: $pluginName, 错误: ${e.message}")
            }
        }
    }
}
