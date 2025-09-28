package editorx.vfs

import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Virtual file abstraction inspired by IntelliJ's VirtualFile.
 * Can represent local files or other schemes in the future.
 */
interface VirtualFile {
    val name: String
    val path: String
    val extension: String
    val isDirectory: Boolean
    val length: Long
    val lastModified: Long

    fun inputStream(): InputStream
    fun readText(charset: Charset = StandardCharsets.UTF_8): String

    fun parent(): VirtualFile?
    fun children(): List<VirtualFile>
}

/** Simple LocalFile implementation backed by java.io.File. */
class LocalVirtualFile(private val delegate: File) : VirtualFile {
    override val name: String get() = delegate.name
    override val path: String get() = delegate.absolutePath
    override val extension: String get() = delegate.extension
    override val isDirectory: Boolean get() = delegate.isDirectory
    override val length: Long get() = delegate.length()
    override val lastModified: Long get() = delegate.lastModified()

    override fun inputStream(): InputStream = delegate.inputStream()

    override fun readText(charset: Charset): String = delegate.readText(charset)

    override fun parent(): VirtualFile? = delegate.parentFile?.let { LocalVirtualFile(it) }

    override fun children(): List<VirtualFile> =
        if (delegate.isDirectory) delegate.listFiles()?.map { LocalVirtualFile(it) } ?: emptyList()
        else emptyList()

    fun toFile(): File = delegate

    companion object {
        fun of(file: File): LocalVirtualFile = LocalVirtualFile(file)
    }
}

