package editorx.plugins.smali

import editorx.lang.Language

class SmaliLanguage : Language("smali") {
    companion object {
        private val INSTANCE: SmaliLanguage = SmaliLanguage()

        fun getInstance(): SmaliLanguage = INSTANCE
    }
}