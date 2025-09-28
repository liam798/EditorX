import editorx.lang.Language

class XmlLanguage : Language("xml") {
    companion object {
        private val INSTANCE: XmlLanguage = XmlLanguage()

        fun getInstance(): XmlLanguage = INSTANCE
    }
}
