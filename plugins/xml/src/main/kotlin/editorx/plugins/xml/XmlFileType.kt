import editorx.filetype.LanguageFileType
import editorx.gui.IconRef
import editorx.plugins.xml.XmlIcons

object XmlFileType : LanguageFileType(XmlLanguage) {
    const val DEFAULT_EXTENSION: String = "xml"

    override fun getName(): String = "xml"

    override fun getDescription(): String = "XML files"

    override fun getDefaultExtension(): String = DEFAULT_EXTENSION

    override fun getIcon(): IconRef = XmlIcons.XmlFile
}
