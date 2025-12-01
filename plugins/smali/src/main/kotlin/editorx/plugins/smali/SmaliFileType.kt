package editorx.plugins.smali

import editorx.filetype.LanguageFileType
import javax.swing.Icon

object SmaliFileType : LanguageFileType(SmaliLanguage) {
    override fun getName(): String = "smali"

    override fun getExtensions(): Array<String> = arrayOf("smali")

    override fun getIcon(): Icon? = SmaliIcons.SmaliFile
}
