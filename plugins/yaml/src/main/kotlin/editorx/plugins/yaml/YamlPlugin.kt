package editorx.plugins.yaml

import YamlFileType
import editorx.plugin.Plugin
import editorx.plugin.PluginContext
import editorx.plugin.PluginInfo

class YamlPlugin : Plugin {

    override fun getInfo(): PluginInfo = PluginInfo(
        id = "yaml",
        name = "YAML",
        version = "0.0.1",
    )

    override fun activate(context: PluginContext) {
        context.registerFileType(YamlFileType)
    }
}
