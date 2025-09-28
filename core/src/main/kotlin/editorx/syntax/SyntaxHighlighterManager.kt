package editorx.syntax

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 语法高亮管理器
 * 负责管理所有语法高亮提供者
 */
object SyntaxHighlighterManager {
    private val providers = mutableListOf<SyntaxHighlighterProvider>()
    private val fileToProviderCache = ConcurrentHashMap<String, SyntaxHighlighterProvider?>()

    /**
     * 注册语法适配器
     */
    fun registerProvider(adapter: SyntaxHighlighterProvider) {
        providers.add(adapter)

        // 注册 TokenMaker
        val tmf = TokenMakerFactory.getDefaultInstance() as AbstractTokenMakerFactory
        tmf.putMapping(adapter.syntaxStyleKey, adapter.getTokenMakerClassName())
    }

    /**
     * 获取文件对应的语法适配器
     */
    fun getProviderForFile(file: File): SyntaxHighlighterProvider? {
        val cacheKey = file.absolutePath
        return fileToProviderCache.computeIfAbsent(cacheKey) {
            val ext = file.extension.lowercase()
            return@computeIfAbsent providers.find { ext in it.fileExtensions }
        }
    }
}
