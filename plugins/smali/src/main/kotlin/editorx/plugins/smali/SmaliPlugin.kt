package editorx.plugins.smali

import editorx.navigation.NavigationProvider
import editorx.navigation.NavigationTarget
import editorx.plugin.Plugin
import editorx.plugin.PluginContext
import editorx.plugin.PluginInfo
import editorx.syntax.SyntaxHighlighterProvider
import editorx.vfs.VirtualFile

class SmaliPlugin : Plugin {
    override fun getInfo(): PluginInfo {
        return PluginInfo(
            id = "smali",
            name = "Smali",
            version = "0.0.1",
        )
    }

    override fun activate(context: PluginContext) {
        // 注册文件类型
        context.registerFileType(SmaliFileType())

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

        // 注册简单的跳转处理器（占位实现，可由插件完善）
        context.registerNavigationProvider(object : NavigationProvider {
            override fun supports(file: VirtualFile): Boolean =
                file.extension.equals("smali", ignoreCase = true)

            override fun gotoDefinition(file: VirtualFile, offset: Int, contents: String): NavigationTarget? {
                // 占位：暂不实现具体跳转逻辑
                return null
            }
        })
    }
}
