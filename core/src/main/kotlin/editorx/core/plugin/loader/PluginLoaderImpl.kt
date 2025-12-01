package editorx.core.plugin.loader

import editorx.core.plugin.Plugin
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import java.util.logging.Logger


class PluginLoaderImpl : PluginLoader {

    private val logger = Logger.getLogger(PluginLoaderImpl::class.java.name)

    override fun load(): List<Plugin> {
        val map = mutableMapOf<Class<out Plugin>, Plugin>()

        loadFromClsLoader(map, thisClassLoader())
        loadInstalledPlugins(map)

        val list = map.values.toList().sortedBy { it.javaClass.simpleName }
        return list
    }

    private fun loadFromClsLoader(map: MutableMap<Class<out Plugin>, Plugin>, classLoader: ClassLoader) {
        ServiceLoader.load(Plugin::class.java, classLoader)
            .stream()
            .filter { p: ServiceLoader.Provider<Plugin> -> p.type().classLoader === classLoader }
            .filter { p: ServiceLoader.Provider<Plugin> -> !map.containsKey(p.type()) }
            .forEach { p: ServiceLoader.Provider<Plugin> -> map[p.type()] = p.get() }
    }

    private fun loadInstalledPlugins(map: MutableMap<Class<out Plugin>, Plugin>) {
        val pluginDir = Path.of("plugins")
        if (!pluginDir.toFile().exists()) {
            pluginDir.toFile().mkdirs()
            logger.info("插件目录不存在，已创建: ${pluginDir.toFile().absolutePath}")
        } else {
            pluginDir.toFile().listFiles()?.forEach { jar ->
                loadFromJar(map, jar.toPath())
            }
        }
    }

    private fun loadFromJar(map: MutableMap<Class<out Plugin>, Plugin>, jar: Path) {
        try {
            val jarFile = jar.toFile()
            val clsLoaderName = "jadx-plugin:" + jarFile.name
            val urls: Array<URL> = arrayOf(jarFile.toURI().toURL())
            val pluginClsLoader = URLClassLoader(clsLoaderName, urls, thisClassLoader())
            loadFromClsLoader(map, pluginClsLoader)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load plugins from jar: $jar", e)
        }
    }

    private fun thisClassLoader() = this::class.java.classLoader
}
