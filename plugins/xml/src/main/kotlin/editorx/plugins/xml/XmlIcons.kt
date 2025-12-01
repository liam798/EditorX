package editorx.plugins.xml

import editorx.core.util.IconRef
import editorx.core.util.IconLoader
import javax.swing.Icon

object XmlIcons {
    val XmlFile: Icon? = load("icons/xml.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, XmlIcons::class.java.classLoader))
    }
}