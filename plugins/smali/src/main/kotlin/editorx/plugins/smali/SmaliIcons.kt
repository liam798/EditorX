package editorx.plugins.smali

import editorx.core.util.IconRef
import editorx.core.util.IconLoader
import javax.swing.Icon

object SmaliIcons {
    val SmaliFile: Icon? = load("icons/smali.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, SmaliIcons::class.java.classLoader))
    }
}