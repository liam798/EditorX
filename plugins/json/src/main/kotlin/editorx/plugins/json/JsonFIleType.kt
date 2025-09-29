package editorx.plugins.json

import editorx.filetype.LanguageFileType
import editorx.gui.IconRef
import javax.swing.Icon

object JsonFIleType : LanguageFileType(JsonLanguage) {

    override fun getName(): String = "json"

    override fun getExtensions(): Array<String> = arrayOf("json")

    override fun getIcon(): Icon? = JsonIcons.JsonFile
}