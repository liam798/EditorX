package editorx.core.util

/**
 * A lightweight, GUI-agnostic icon reference.
 *
 * GUI layer is responsible for loading/rendering the icon from this reference.
 */
data class IconRef(
    val resourcePath: String,
    val classLoader: ClassLoader? = null,
) {
    val isSvg: Boolean get() = resourcePath.endsWith(".svg", ignoreCase = true)
}

