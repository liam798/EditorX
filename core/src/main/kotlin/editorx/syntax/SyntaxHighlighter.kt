package editorx.syntax

interface SyntaxHighlighter {

    /**
     * 获取 TokenMaker 类名
     */
    fun getTokenMakerClassName(): String

    /**
     * 是否支持代码折叠
     */
    fun supportsFolding(): Boolean

    /**
     * 是否启用括号匹配
     */
    fun isBracketMatchingEnabled(): Boolean
}
