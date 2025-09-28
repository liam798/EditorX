import editorx.gui.IconRef
import editorx.lang.LanguageFileType

class XmlFileType : LanguageFileType(XmlLanguage.getInstance()) {
    override fun getName(): String {
        return "xml"
    }

    override fun getDescription(): String {
        return "XML files"
    }

    override fun getExtensions(): Set<String> {
        return setOf("xml")
    }

    override fun getIcon(): IconRef {
        return IconRef("icons/xml.svg", XmlFileType::class.java.classLoader)
    }
}
