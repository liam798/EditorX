package editorx.core.filetype

import editorx.core.lang.Language

abstract class LanguageFileType(
    val language: Language,
) : FileType {

    override fun getDisplayName(): String {
        return language.getDisplayName()
    }
}

