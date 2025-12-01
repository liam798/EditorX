package editorx.gui

import editorx.core.settings.PropertiesSettingsStore
import editorx.core.settings.SettingsStore
import editorx.core.workspace.DefaultWorkspaceManager
import editorx.core.workspace.WorkspaceManager
import java.io.File

class GuiEnvironment(private val appDir: File) {
    val settings: SettingsStore by lazy {
        val settingsFile = File(appDir, "settings.properties")
        PropertiesSettingsStore(settingsFile)
    }
    val workspace: editorx.core.workspace.WorkspaceManager = editorx.core.workspace.DefaultWorkspaceManager(settings)
}
