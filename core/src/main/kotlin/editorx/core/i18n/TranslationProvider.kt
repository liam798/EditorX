package editorx.core.i18n

/**
 * 翻译提供器：提供单个 key 的翻译。
 *
 * 约定：
 * - 每个 TranslationProvider 只对应一种语言（在注册时指定）；
 * - 如果 key 不存在，返回 null；
 * - key 建议使用点分层级（如 "menu.file" / "action.find"）。
 */
fun interface TranslationProvider {
    fun translate(key: String): String?
}

