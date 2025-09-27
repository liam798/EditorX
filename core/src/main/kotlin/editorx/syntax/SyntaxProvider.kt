package editorx.syntax

interface SyntaxProvider {
    val syntaxStyleKey: String           // e.g., "text/smali"
    val fileExtensions: Set<String>  // e.g., setOf(".smali")

    fun getSyntaxHighlighter(): SyntaxHighlighter
}

abstract class CachedSyntaxProvider : SyntaxProvider {
    private var cachedSyntaxHighlighter: SyntaxHighlighter? = null

    override fun getSyntaxHighlighter(): SyntaxHighlighter {
        if (cachedSyntaxHighlighter == null) {
            synchronized(this) {
                if (cachedSyntaxHighlighter == null) {
                    cachedSyntaxHighlighter = createSyntaxHighlighter()
                }
            }
        }
        return cachedSyntaxHighlighter!!
    }

    abstract fun createSyntaxHighlighter(): SyntaxHighlighter
}
