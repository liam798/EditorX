import editorx.filetype.LanguageFileType
import editorx.plugins.xml.XmlIcons
import javax.swing.Icon

object XmlFileType : LanguageFileType(XmlLanguage) {
    override fun getName(): String = "xml"

    override fun getExtensions(): Array<String> = arrayOf("xml")

    override fun getIcon(): Icon? = XmlIcons.XmlFile
}
