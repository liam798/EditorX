package editorx.lang

import java.util.concurrent.ConcurrentHashMap

object LanguageRegistry {
    private val languages = ConcurrentHashMap<String, Language>()

    @Synchronized
    fun register(language: Language) {
        if (!languages.containsKey(language.id)) languages[language.id] = language
    }

    fun get(id: String): Language? = languages[id]
    fun all(): List<Language> = languages.values.toList()
}

