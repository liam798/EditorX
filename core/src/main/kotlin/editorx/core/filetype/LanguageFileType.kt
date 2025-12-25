package editorx.core.filetype

abstract class LanguageFileType(
    val language: Language,
) : FileType {

    override fun getDisplayName(): String {
        return language.getDisplayName()
    }
}

