package editorx.plugins.yaml

import editorx.util.IconRef
import editorx.util.IconLoader
import javax.swing.Icon

object YamlIcons {
    val YamlFile: Icon? = load("icons/yaml.svg")

    private fun load(path: String): Icon? {
        return IconLoader.getIcon(IconRef(path, YamlIcons::class.java.classLoader))
    }
}