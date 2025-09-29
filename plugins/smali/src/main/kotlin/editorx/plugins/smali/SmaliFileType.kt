package editorx.plugins.smali

import editorx.filetype.LanguageFileType
import editorx.gui.IconRef

object SmaliFileType : LanguageFileType(SmaliLanguage) {
    const val DEFAULT_EXTENSION: String = "smali"

    override fun getName(): String = "smali"

    override fun getDescription(): String = "Smali assembly files"

    override fun getDefaultExtension(): String = DEFAULT_EXTENSION

    override fun getIcon(): IconRef = SmaliIcons.SmaliFile
}
