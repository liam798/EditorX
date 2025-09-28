package editorx.plugins.smali

import editorx.gui.IconRef
import editorx.lang.LanguageFileType

class SmaliFileType : LanguageFileType(
    language = SmaliLanguage.getInstance(),
) {
    override fun getName(): String {
        return "smali"
    }

    override fun getDescription(): String {
        return "Smali assembly files"
    }

    override fun getExtensions(): Set<String> {
        return setOf("smali")
    }

    override fun getIcon(): IconRef {
        return IconRef("icons/smali.svg", SmaliFileType::class.java.classLoader)
    }
}
