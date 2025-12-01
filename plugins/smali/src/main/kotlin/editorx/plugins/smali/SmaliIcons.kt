package editorx.plugins.smali

import editorx.util.IconRef
import editorx.util.IconLoader
import javax.swing.Icon

object SmaliIcons {
    val SmaliFile: Icon? = load("icons/smali.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, SmaliIcons::class.java.classLoader))
    }
}