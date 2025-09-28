package editorx.gui.plugin

import editorx.gui.ViewProvider
import editorx.file.FileType
import editorx.file.FileTypeRegistry
import editorx.navigation.NavigationProvider
import editorx.navigation.NavigationRegistry
import editorx.gui.main.MainWindow
import editorx.plugin.LoadedPlugin
import editorx.plugin.PluginContext
import editorx.settings.SettingsStore
import editorx.syntax.SyntaxHighlighterManager
import editorx.syntax.SyntaxHighlighterProvider
import editorx.workspace.WorkspaceManager
import java.util.logging.Logger

/**
 * GUI 实现的插件上下文
 * 为每个插件创建独立的实例，包含插件标识信息
 */
class GuiPluginContext(
    private val mainWindow: MainWindow,
    private val loadedPlugin: LoadedPlugin,
) : PluginContext {

    private val logger =
        Logger.getLogger("${GuiPluginContext::class.java.name}[${loadedPlugin.name}-${loadedPlugin.version}]")

    override fun settings(): SettingsStore = mainWindow.guiControl.settings

    override fun workspace(): WorkspaceManager = mainWindow.guiControl.workspace

    override fun addActivityBarItem(iconPath: String, viewProvider: ViewProvider) {
        mainWindow.activityBar.addItem(loadedPlugin.id, loadedPlugin.name, iconPath, viewProvider)
    }

    override fun registerSyntaxHighlighterProvider(syntaxHighlighterProvider: SyntaxHighlighterProvider) {
        SyntaxHighlighterManager.registerProvider(syntaxHighlighterProvider)
    }

    override fun registerFileType(fileType: FileType) {
        FileTypeRegistry.register(fileType)
    }

    override fun registerNavigationProvider(provider: NavigationProvider) {
        NavigationRegistry.register(provider)
    }
}
