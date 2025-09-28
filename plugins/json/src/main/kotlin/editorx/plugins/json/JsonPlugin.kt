package editorx.plugins.json

import editorx.plugin.Plugin
import editorx.plugin.PluginContext
import editorx.plugin.PluginInfo

class JsonPlugin : Plugin {
    override fun getInfo(): PluginInfo = PluginInfo(
        id = "json",
        name = "JSON",
        version = "0.0.1",
    )

    override fun activate(context: PluginContext) {
        context.registerFileType(JsonFIleType())
    }
}

