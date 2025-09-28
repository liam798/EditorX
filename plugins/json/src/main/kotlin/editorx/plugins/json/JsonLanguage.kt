package editorx.plugins.json

import editorx.lang.Language

class JsonLanguage : Language("json") {
    companion object {
        private val INSTANCE: JsonLanguage = JsonLanguage()

        fun getInstance(): JsonLanguage = INSTANCE
    }
}