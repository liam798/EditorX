package editorx.core.plugin

import editorx.core.plugin.gui.GuiContext

class PluginContextImpl(private val plugin: Plugin) : PluginContext {
    private var guiContext: GuiContext? = null

    override fun pluginInfo(): PluginInfo {
        return plugin.getInfo()
    }

    override fun gui(): GuiContext? {
        return guiContext
    }

    fun setGuiContext(guiContext: GuiContext) {
        this.guiContext = guiContext
    }
}