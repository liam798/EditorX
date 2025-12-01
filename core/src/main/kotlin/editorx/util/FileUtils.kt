package editorx.util

object FileUtils {

    fun getExtension(fileName: String): String {
        val index = fileName.lastIndexOf('.')
        if (index < 0) return ""
        return fileName.substring(index + 1)
    }
}