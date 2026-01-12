package editorx.gui.update

import editorx.core.util.AppPaths
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

object VersionUtil {
    private val logger = LoggerFactory.getLogger(VersionUtil::class.java)

    fun currentVersion(): String {
        System.getProperty("editorx.version")?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }

        // macOS .app：Info.plist 位于 <Contents>/Info.plist
        val infoPlist = AppPaths.appHome().resolve("Info.plist").toFile()
        parseInfoPlistVersion(infoPlist)?.let { return it }

        // 资源文件兜底（若未来接入 Gradle 生成）
        val cl = Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader()
        runCatching { cl.getResourceAsStream("editorx-version.txt") }
            .getOrNull()
            ?.use { it.bufferedReader().readText().trim() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        return "dev"
    }

    fun isNewer(current: String, latest: String): Boolean {
        val c = normalizeVersion(current)
        val l = normalizeVersion(latest)
        if (c == null || l == null) {
            // 版本无法解析：只要不相等就认为有更新（避免漏提示）
            return current.trim() != latest.trim()
        }
        val max = maxOf(c.size, l.size)
        for (i in 0 until max) {
            val cv = c.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        return false
    }

    fun normalizeTag(tagOrVersion: String): String {
        return tagOrVersion.trim().removePrefix("v").removePrefix("V")
    }

    private fun normalizeVersion(raw: String): List<Int>? {
        val s = normalizeTag(raw)
        if (s.isEmpty()) return null
        val parts = s.split('.', '-', '_')
            .mapNotNull { p -> p.trim().takeIf { it.isNotEmpty() } }
        if (parts.isEmpty()) return null
        val nums = mutableListOf<Int>()
        for (p in parts) {
            val n = p.toIntOrNull() ?: break
            nums += n
        }
        if (nums.isEmpty()) return null
        return nums
    }

    private fun parseInfoPlistVersion(infoPlist: File): String? {
        if (!infoPlist.exists() || !infoPlist.isFile) return null
        val text = runCatching { infoPlist.readText() }
            .onFailure { e -> logger.debug("读取 Info.plist 失败: {}", infoPlist.absolutePath, e) }
            .getOrNull()
            ?: return null

        // 极简解析：查找 CFBundleShortVersionString 对应的 <string> 值
        val keyIndex = text.indexOf("CFBundleShortVersionString")
        if (keyIndex < 0) return null
        val after = text.substring(keyIndex)
        val m = Regex("""<string>\s*([^<\n\r]+)\s*</string>""").find(after) ?: return null
        return m.groupValues.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun appBundlePathOrNull(): Path? {
        val home = AppPaths.appHome()
        val parent = home.parent ?: return null
        // 预期 home 为 .../EditorX.app/Contents
        if (home.fileName?.toString() != "Contents") return null
        if (!parent.fileName.toString().endsWith(".app")) return null
        return parent
    }

    fun ensureUpdateCacheDir(): Path {
        val dir = Path.of(System.getProperty("user.home"), ".editorx", "updates")
        Files.createDirectories(dir)
        return dir
    }
}

