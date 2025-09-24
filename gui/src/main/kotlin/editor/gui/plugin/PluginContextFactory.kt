package editor.gui.plugin

import editor.gui.ui.MainWindow
import editor.plugin.PluginContext

class PluginContextFactory(private val mainWindow: MainWindow) {

    fun createPluginContext(): PluginContext {
        return GuiPluginContext(mainWindow)
    }
}
