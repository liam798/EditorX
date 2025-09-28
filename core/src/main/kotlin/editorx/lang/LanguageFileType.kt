package editorx.lang

import editorx.file.FileType

abstract class LanguageFileType(
    val language: Language,
) : FileType {

    override fun getDisplayName(): String {
        return language.getDisplayName()
    }

    override fun isBinary(): Boolean {
        return false
    }
}

