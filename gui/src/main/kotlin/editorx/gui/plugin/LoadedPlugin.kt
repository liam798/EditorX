package editorx.gui.plugin

import editorx.plugin.Plugin
import java.io.File
import java.net.URLClassLoader

data class LoadedPlugin(
    val plugin: Plugin,
    val id: String,
    val name: String,
    val version: String,
    val jarFile: File? = null,
    val loader: URLClassLoader? = null
)
