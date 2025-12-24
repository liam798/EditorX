package editorx.gui.main.explorer

import editorx.core.util.IconRef
import editorx.core.util.IconLoader
import javax.swing.Icon

object ExplorerIcons {
    val Folder: Icon? = load("icons/folder.svg")
    val AnyType: Icon? = load("icons/anyType.svg")
    val ResourcesRoot: Icon? = load("icons/resourcesRoot.svg")
    val SourceRoot: Icon? = load("icons/sourceRoot.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, ExplorerIcons::class.java.classLoader))
    }
}