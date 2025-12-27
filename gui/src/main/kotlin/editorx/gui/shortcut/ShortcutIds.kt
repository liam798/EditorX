package editorx.gui.shortcut

/**
 * 快捷键 ID 常量
 * 集中管理所有快捷键的唯一标识符，避免字符串拼写错误
 */
object ShortcutIds {
    /**
     * 全局快捷键
     */
    object Global {
        const val SEARCH = "global.search"
        const val SETTINGS = "global.settings"
    }

    /**
     * 编辑器快捷键
     */
    object Editor {
        const val NEW_FILE = "editor.newFile"
        const val CLOSE_TAB = "editor.closeTab"
        const val FORMAT_FILE = "editor.formatFile"
    }
}

