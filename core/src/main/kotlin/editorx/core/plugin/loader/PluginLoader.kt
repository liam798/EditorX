package editorx.core.plugin.loader

import editorx.core.plugin.Plugin

interface PluginLoader {

    fun load(): List<Plugin>
}
