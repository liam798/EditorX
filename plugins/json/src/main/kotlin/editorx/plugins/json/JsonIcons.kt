package editorx.plugins.json

import editorx.gui.IconRef
import editorx.util.IconLoader
import javax.swing.Icon

object JsonIcons {
    val JsonFile: Icon? = load("icons/json.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, JsonIcons::class.java.classLoader))
    }
}