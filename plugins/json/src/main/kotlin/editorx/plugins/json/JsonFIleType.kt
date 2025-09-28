package editorx.plugins.json

import editorx.gui.IconRef
import editorx.lang.LanguageFileType
import editorx.vfs.VirtualFile

class JsonFIleType : LanguageFileType(JsonLanguage.getInstance()) {
    override fun getName(): String = "json"

    override fun getDescription(): String = "JSON files"

    override fun getExtensions(): Set<String> = setOf("json")

    override fun getIcon(): IconRef = IconRef("icons/json.svg", this@JsonFIleType::class.java.classLoader)

    override fun isMyFile(file: VirtualFile): Boolean = file.extension.equals("json", ignoreCase = true)
}