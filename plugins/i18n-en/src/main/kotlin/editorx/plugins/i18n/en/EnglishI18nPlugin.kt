package editorx.plugins.i18n.en

import editorx.core.i18n.I18nKeys
import editorx.core.i18n.I18nPlugin
import editorx.core.plugin.PluginInfo
import java.util.Locale

private val dictionary = mapOf(
    I18nKeys.Menu.FILE to "File",
    I18nKeys.Menu.EDIT to "Edit",
    I18nKeys.Menu.PLUGINS to "Plugins",
    I18nKeys.Menu.HELP to "Help",
    I18nKeys.Menu.LANGUAGE to "Language",

    I18nKeys.Action.OPEN_FILE to "Open File…",
    I18nKeys.Action.OPEN_FOLDER to "Open Folder…",
    I18nKeys.Action.RECENT to "Recent Files",
    I18nKeys.Action.SAVE to "Save",
    I18nKeys.Action.SAVE_AS to "Save As…",
    I18nKeys.Action.EXIT to "Quit",
    I18nKeys.Action.UNDO to "Undo",
    I18nKeys.Action.REDO to "Redo",
    I18nKeys.Action.FIND to "Find…",
    I18nKeys.Action.REPLACE to "Replace…",
    I18nKeys.Action.GLOBAL_SEARCH to "Search in Files…",
    I18nKeys.Action.PLUGIN_MANAGER to "Plugin Manager",
    I18nKeys.Action.ABOUT to "About",
    I18nKeys.Action.HELP to "Help",
    I18nKeys.Action.NEW_FILE to "New File",
    I18nKeys.Action.NEW_FOLDER to "New Folder",
    I18nKeys.Action.DELETE to "Delete",
    I18nKeys.Action.REFRESH to "Refresh",
    I18nKeys.Action.REVEAL_IN_SYSTEM to "Reveal in System",
    I18nKeys.Action.COPY_PATH to "Copy Path",
    I18nKeys.Action.SELECT_IN_SIDEBAR to "Select in Sidebar",
    I18nKeys.Action.REVEAL_IN_EXPLORER to "Reveal in Explorer",
    I18nKeys.Action.CLOSE to "Close",
    I18nKeys.Action.CLOSE_OTHERS to "Close Others",
    I18nKeys.Action.CLOSE_ALL to "Close All",
    I18nKeys.Action.CLOSE_LEFT to "Close Left",
    I18nKeys.Action.CLOSE_RIGHT to "Close Right",
    I18nKeys.Action.CLOSE_UNMODIFIED to "Close Unmodified",
    I18nKeys.Action.FORMAT_FILE to "Format File",
    I18nKeys.Action.RESET to "Reset",
    I18nKeys.Action.CANCEL to "Cancel",
    I18nKeys.Action.CONFIRM to "Confirm",
    I18nKeys.Action.INSTALL_PLUGIN to "Install Plugin",
    I18nKeys.Action.OPEN_PLUGINS_FOLDER to "Open Plugins Folder",
    I18nKeys.Action.ENABLE to "Enable",
    I18nKeys.Action.DISABLE to "Disable",
    I18nKeys.Action.UNINSTALL to "Uninstall",

    I18nKeys.Settings.TITLE to "Preferences",
    I18nKeys.Settings.PREFERENCES to "Preferences",
    I18nKeys.Settings.APPEARANCE to "Appearance",
    I18nKeys.Settings.LANGUAGE to "Language",
    I18nKeys.Settings.THEME to "Theme",
    I18nKeys.Settings.APPEARANCE_TIP to "Tip: language and theme changes apply immediately.",
    I18nKeys.Settings.KEYMAP to "Keymap",
    I18nKeys.Settings.PLUGINS to "Plugins",
    I18nKeys.Settings.CACHE to "Cache",
    I18nKeys.Settings.KEYMAP_TITLE to "Keymap (Planned)",
    I18nKeys.Settings.KEYMAP_HINT to "<html>Current list shows default shortcuts. Customization/export is under planning.</html>",
    I18nKeys.Settings.ADD_NOTE to "Add Note…",
    I18nKeys.Settings.ADD_NOTE_TOOLTIP to "Shortcut customization is under development",
    I18nKeys.Settings.EXPORT to "Export…",
    I18nKeys.Settings.EXPORT_TOOLTIP to "Feature under development",
    I18nKeys.Settings.CACHE_TITLE to "Cache",
    I18nKeys.Settings.CACHE_HINT to "Clear cache only when no background decompile tasks are running.",
    I18nKeys.Settings.REFRESH_CACHE to "Refresh",
    I18nKeys.Settings.CLEAR_SELECTED to "Clear Selected",
    I18nKeys.Settings.OPEN_FOLDER to "Open Folder",
    I18nKeys.Settings.PLUGIN_STATE_DISABLED to "Disabled",
    I18nKeys.Settings.PLUGIN_STATE_ENABLED to "Enabled",
    I18nKeys.Settings.PLUGIN_STATE_FAILED to "Failed",

    I18nKeys.CacheTable.NAME to "Name",
    I18nKeys.CacheTable.PATH to "Path",
    I18nKeys.CacheTable.SIZE to "Size",
    I18nKeys.CacheTable.DESCRIPTION to "Description",

    I18nKeys.Theme.LIGHT to "Light",
    I18nKeys.Theme.DARK to "Dark",

    I18nKeys.Status.READY to "Ready",
    I18nKeys.Status.CANCEL to "Cancel",
    I18nKeys.Status.GOTO_LINE_COLUMN to "Go to Line/Column",
    I18nKeys.Status.ERROR to "Error",
    I18nKeys.Status.WARNING to "Warning",
    I18nKeys.Status.SUCCESS to "Success",
    I18nKeys.Status.VERSION_CONTROL to "Version Control",
    I18nKeys.Status.NO_FILE_OPENED to "No file opened",
    I18nKeys.Status.LINE_COLUMN to "Line %d, Column %d",

    I18nKeys.FindReplace.SEARCH to "Search",
    I18nKeys.FindReplace.REPLACE to "Replace",
    I18nKeys.FindReplace.EXPAND_REPLACE to "Expand Replace",
    I18nKeys.FindReplace.COLLAPSE_REPLACE to "Collapse Replace",
    I18nKeys.FindReplace.MATCH_CASE to "Match Case",
    I18nKeys.FindReplace.WHOLE_WORD to "Whole Word",
    I18nKeys.FindReplace.REGEX to "Regex",
    I18nKeys.FindReplace.REPLACE_ONE to "Replace",
    I18nKeys.FindReplace.REPLACE_ALL to "Replace All",
    I18nKeys.FindReplace.REPLACE_ONE_TOOLTIP to "Replace current match",
    I18nKeys.FindReplace.REPLACE_ALL_TOOLTIP to "Replace all matches",
    I18nKeys.FindReplace.FIND_PREV to "Previous (Shift+Enter)",
    I18nKeys.FindReplace.FIND_NEXT to "Next (Enter)",
    I18nKeys.FindReplace.CLOSE to "Close",
    I18nKeys.FindReplace.NO_RESULTS to "0 results",
    I18nKeys.FindReplace.RESULTS to "%d/%d",
    I18nKeys.FindReplace.INVALID_REGEX to "Invalid regex",
    I18nKeys.FindReplace.SEARCH_FAILED to "Search failed",
    I18nKeys.FindReplace.REPLACE_FAILED to "Replace failed",

    I18nKeys.Dialog.SELECT_FOLDER to "Select Folder",
    I18nKeys.Dialog.TIP to "Tip",
    I18nKeys.Dialog.ABOUT_TITLE to "About EditorX",
    I18nKeys.Dialog.ABOUT_MESSAGE to """EditorX v1.0

A tool for editing APK files

Features:
• Syntax highlighting editor
• Plugin system support
• Multi-tab interface
• File browsing and management

Developed by: XiaMao Tools""",
    I18nKeys.Dialog.HELP_NOT_IMPLEMENTED to "Help documentation not implemented",
    I18nKeys.Dialog.NO_RECENT_FILES to "(None)",
    I18nKeys.Dialog.FILE_NOT_EXISTS to "File does not exist",
    I18nKeys.Dialog.NOT_FOUND to "Not found",
    I18nKeys.Dialog.ERROR to "Error",
    I18nKeys.Dialog.INFO to "Info",
    I18nKeys.Dialog.SELECT_ENTRY_FIRST to "Select an entry first.",
    I18nKeys.Dialog.DIRECTORY_NOT_FOUND to "Directory not found: %s",
    I18nKeys.Dialog.CLEAR_CACHE to "Clear Cache",
    I18nKeys.Dialog.CLEARED to "Cleared",
    I18nKeys.Dialog.CLEAR_FAILED to "Failed to clear, files may be in use.",
    I18nKeys.Dialog.UNABLE_TO_OPEN to "Unable to open: %s",

    I18nKeys.Explorer.TITLE to "Explorer",
    I18nKeys.Explorer.NEW_FILE to "New File",
    I18nKeys.Explorer.NEW_FOLDER to "New Folder",
    I18nKeys.Explorer.DELETE to "Delete",
    I18nKeys.Explorer.REFRESH to "Refresh",
    I18nKeys.Explorer.REVEAL_IN_SYSTEM to "Reveal in System",

    I18nKeys.Editor.CLOSE to "Close",
    I18nKeys.Editor.CLOSE_OTHERS to "Close Others",
    I18nKeys.Editor.CLOSE_ALL to "Close All",
    I18nKeys.Editor.CLOSE_LEFT to "Close Left",
    I18nKeys.Editor.CLOSE_RIGHT to "Close Right",
    I18nKeys.Editor.CLOSE_UNMODIFIED to "Close Unmodified",
    I18nKeys.Editor.FORMAT_FILE to "Format File",
    I18nKeys.Editor.CANNOT_READ_FILE to "Cannot read file: %s",
    I18nKeys.Editor.TOTAL_FILES to "Total %d files/directories",
    I18nKeys.Editor.CANNOT_READ_ARCHIVE to "Cannot read archive",

    I18nKeys.Toolbar.GOTO_MANIFEST to "Go to AndroidManifest.xml",
    I18nKeys.Toolbar.GOTO_MAIN_ACTIVITY to "Go to MainActivity",
    I18nKeys.Toolbar.GOTO_APPLICATION to "Go to Application",
    I18nKeys.Toolbar.BUILD to "Build",
    I18nKeys.Toolbar.TOGGLE_SIDEBAR to "Toggle Sidebar",
    I18nKeys.Toolbar.GLOBAL_SEARCH to "Global Search",
    I18nKeys.Toolbar.SETTINGS to "Settings",
    I18nKeys.Toolbar.DOUBLE_SHIFT to "Double Shift",

    I18nKeys.Search.SEARCH to "Search",
    I18nKeys.Search.STOP to "Stop",
    I18nKeys.Search.SEARCH_LABEL to "Search:",
    I18nKeys.Search.PLEASE_ENTER_SEARCH to "Please enter search content",
    I18nKeys.Search.PLEASE_OPEN_FOLDER to "Please open a folder (workspace) first before global search",
    I18nKeys.Search.SCANNED_FILES to "Scanned %d files, found %d results",

    I18nKeys.Welcome.RECENT_PROJECTS to "Recent projects",
    I18nKeys.Welcome.VIEW_ALL to "View all (%d)",
    I18nKeys.Welcome.NO_RECENT_PROJECTS to "No recent projects",

    // 语言名称使用动态生成的 key
    I18nKeys.Lang.forLocale(Locale.forLanguageTag("zh")) to "Chinese",
    I18nKeys.Lang.forLocale(Locale.SIMPLIFIED_CHINESE) to "Chinese (Simplified)",
    I18nKeys.Lang.forLocale(Locale.TRADITIONAL_CHINESE) to "Chinese (Traditional)",
    I18nKeys.Lang.forLocale(Locale.ENGLISH) to "English",
)

class EnglishI18nPlugin : I18nPlugin(Locale.ENGLISH) {

    override fun getInfo() = PluginInfo(
        id = "i18n-en",
        name = "English (i18n)",
        version = "0.0.1",
    )

    override fun translate(key: String): String? = dictionary[key]
}

