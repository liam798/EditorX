package editorx.plugins.xml

import XmlFileType
import editorx.plugin.Plugin
import editorx.plugin.PluginContext
import editorx.plugin.PluginInfo

class XmlPlugin : Plugin {
    override fun getInfo(): PluginInfo = PluginInfo(
        id = "xml",
        name = "XML",
        version = "0.0.1",
    )

    override fun activate(context: PluginContext) {
        context.registerFileType(XmlFileType)
    }
}

