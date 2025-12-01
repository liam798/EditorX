package editorx.plugins.xml

import editorx.util.IconRef
import editorx.util.IconLoader
import javax.swing.Icon

object XmlIcons {
    val XmlFile: Icon? = load("icons/xml.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, XmlIcons::class.java.classLoader))
    }
}