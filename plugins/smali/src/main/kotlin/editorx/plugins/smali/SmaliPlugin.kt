package editorx.plugins.smali

import editorx.plugin.Plugin
import editorx.plugin.PluginContext
import editorx.plugin.PluginInfo
import editorx.syntax.SyntaxHighlighterProvider

class SmaliPlugin : Plugin {
    override fun getInfo(): PluginInfo = PluginInfo(
        id = "smali",
        name = "Smali",
        version = "0.0.1",
    )

    override fun activate(context: PluginContext) {
        // 注册文件类型
        context.registerFileType(SmaliFileType)

        // 注册 Smali 语法高亮提供者
        context.registerSyntaxHighlighterProvider(object : SyntaxHighlighterProvider {
            override val syntaxStyleKey: String = "text/smali"
            override val fileExtensions: Set<String> = setOf("smali")
            override val isCodeFoldingEnabled: Boolean = true
            override val isBracketMatchingEnabled: Boolean = true

            override fun getTokenMakerClassName(): String {
                return SmaliTokenMaker::class.java.name
            }
        })
    }
}
