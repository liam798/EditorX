package editorx.core.plugin.gui

import editorx.core.filetype.FileType
import editorx.core.filetype.SyntaxHighlighter
import editorx.core.gui.GuiViewProvider
import editorx.core.lang.Language

interface GuiContext {

    /**
     * 在 ActivityBar 注册一个入口按钮，并指定视图提供器
     */
    fun addActivityBarItem(iconPath: String, viewProvider: GuiViewProvider)

    /**
     * 注册文件类型
     */
    fun registerFileType(fileType: FileType)

    /**
     * 注册语法高亮
     */
    fun registerSyntaxHighlighter(language: Language, syntaxHighlighter: SyntaxHighlighter)
}
