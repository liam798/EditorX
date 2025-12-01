package editorx.core.util

import java.net.URL
import javax.swing.Icon
import javax.swing.ImageIcon

object IconLoader {
    fun getIcon(iconRef: IconRef, size: Int = 16): Icon? {
        return if (iconRef.isSvg) loadSvg(iconRef, size) else loadRaster(iconRef, size)
    }

    private fun loadRaster(iconRef: IconRef, size: Int): Icon? {
        val cl = iconRef.classLoader ?: IconLoader::class.java.classLoader
        val normalized =
            if (iconRef.resourcePath.startsWith("/")) iconRef.resourcePath.substring(1) else iconRef.resourcePath
        val url = cl.getResource(normalized) ?: return null
        return IconUtils.resizeIcon(ImageIcon(url), size, size)
    }

    private fun loadSvg(iconRef: IconRef, size: Int): Icon? {
        val cl = iconRef.classLoader ?: IconLoader::class.java.classLoader
        val normalized =
            if (iconRef.resourcePath.startsWith("/")) iconRef.resourcePath.substring(1) else iconRef.resourcePath
        val url: URL = cl.getResource(normalized) ?: return SvgIcon.fromResource(
            iconRef.resourcePath,
            iconRef.classLoader,
            size,
            size
        )

        // Prefer FlatLaf's robust SVG renderer if available
        runCatching {
            val clazz = Class.forName("com.formdev.flatlaf.extras.FlatSVGIcon")
            val ctor = clazz.getConstructor(URL::class.java)
            val icon = ctor.newInstance(url) as Icon
            return IconUtils.resizeIcon(icon, size, size)
        }

        // Fallback to plain SVGIcon if present
        runCatching {
            val clazz = Class.forName("com.formdev.svg.SVGIcon")
            val ctor = clazz.getConstructor(URL::class.java)
            val icon = ctor.newInstance(url) as Icon
            return IconUtils.resizeIcon(icon, size, size)
        }

        // Last resort: our minimal SvgIcon parser
        return SvgIcon.fromResource(iconRef.resourcePath, iconRef.classLoader, size, size)
    }
}
