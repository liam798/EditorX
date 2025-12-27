package editorx.core.plugin.loader

import editorx.core.plugin.DiscoveredPlugin

interface PluginLoader {

    fun load(): List<DiscoveredPlugin>
}
