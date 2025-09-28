package editorx.lang

/**
 * Represents a programming or data language (e.g., Kotlin, Smali, JSON).
 */
abstract class Language(val id: String) {

    fun getDisplayName(): String = id
}

