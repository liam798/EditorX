package editorx.filetype

import editorx.lang.Language

abstract class LanguageFileType(
    val language: Language,
) : FileType {

    override fun isBinary(): Boolean {
        return false
    }

    override fun getDisplayName(): String {
        return language.getDisplayName()
    }
}

