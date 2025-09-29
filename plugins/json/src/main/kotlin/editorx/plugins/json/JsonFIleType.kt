package editorx.plugins.json

import editorx.filetype.LanguageFileType
import editorx.gui.IconRef

object JsonFIleType : LanguageFileType(JsonLanguage) {
    const val DEFAULT_EXTENSION = "json"

    override fun getName(): String = "json"

    override fun getDescription(): String = "JSON files"

    override fun getDefaultExtension(): String = DEFAULT_EXTENSION

    override fun getIcon(): IconRef = JsonIcons.JsonFile
}