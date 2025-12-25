package editorx.core.plugin

import editorx.core.plugin.gui.PluginGuiContext
import editorx.core.service.ServiceRegistry

interface PluginContext {

    fun pluginId(): String

    fun pluginInfo(): PluginInfo

    fun gui(): PluginGuiContext?

    fun services(): ServiceRegistry

    fun <T : Any> registerService(serviceClass: Class<T>, instance: T)

    fun <T : Any> unregisterService(serviceClass: Class<T>)
}
