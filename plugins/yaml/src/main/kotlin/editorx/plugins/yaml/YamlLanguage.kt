import editorx.lang.Language

class YamlLanguage : Language("xml") {
    companion object {
        private val INSTANCE: YamlLanguage = YamlLanguage()

        fun getInstance(): YamlLanguage = INSTANCE
    }
}
