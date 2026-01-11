package editorx.gui.update

import editorx.core.util.SystemUtils
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name

object SelfUpdater {
    private val logger = LoggerFactory.getLogger(SelfUpdater::class.java)

    data class ApplyResult(val success: Boolean, val message: String)

    fun applyDownloadedPackage(zipFile: Path, currentPid: Long): ApplyResult {
        if (!zipFile.exists()) return ApplyResult(false, "更新包不存在：${zipFile.absolutePathString()}")

        if (!SystemUtils.isMacOS()) {
            return ApplyResult(false, "当前仅实现 macOS 自动更新（已下载：${zipFile.absolutePathString()}）")
        }

        val bundlePath = VersionUtil.appBundlePathOrNull()
            ?: return ApplyResult(false, "当前不是从 macOS .app 运行，无法自动替换自身（已下载：${zipFile.absolutePathString()}）")

        val parentDir = bundlePath.parent
        if (parentDir == null || !Files.isWritable(parentDir)) {
            return ApplyResult(
                false,
                "应用目录不可写，无法自动更新。\n已下载更新包：${zipFile.absolutePathString()}\n请手动解压并替换当前应用。"
            )
        }

        val extractDir = Files.createTempDirectory("editorx_update_")
        val ditto = Path.of("/usr/bin/ditto")
        if (!ditto.exists()) return ApplyResult(false, "缺少 /usr/bin/ditto，无法解压更新包")

        val unzip = ProcessBuilder(
            ditto.absolutePathString(),
            "-x",
            "-k",
            zipFile.absolutePathString(),
            extractDir.absolutePathString()
        ).start()
        val unzipExit = unzip.waitFor()
        if (unzipExit != 0) {
            val out = unzip.inputStream.bufferedReader().use { it.readText() }
            return ApplyResult(false, "解压失败（exit=$unzipExit）：$out")
        }

        val newApp = Files.list(extractDir).use { stream ->
            stream.filter { it.name.endsWith(".app") && it.isDirectory() }.findFirst().orElse(null)
        } ?: run {
            return ApplyResult(false, "解压后未找到 .app 目录")
        }

        val helper = writeHelperScript(extractDir, currentPid, bundlePath, newApp)
        runCatching {
            ProcessBuilder("/bin/bash", helper.absolutePathString())
                .directory(extractDir.toFile())
                .start()
        }.onFailure { e ->
            logger.warn("启动更新脚本失败", e)
            return ApplyResult(false, "启动更新脚本失败：${e.message}")
        }

        return ApplyResult(true, "正在更新并重启…")
    }

    private fun writeHelperScript(extractDir: Path, pid: Long, currentApp: Path, newApp: Path): Path {
        val script = extractDir.resolve("apply_update.sh")
        val content = """
            #!/bin/bash
            set -e
            
            PID="${pid}"
            CURRENT_APP="${currentApp.absolutePathString()}"
            NEW_APP="${newApp.absolutePathString()}"
            
            # 等待旧进程退出
            while kill -0 "${'$'}PID" >/dev/null 2>&1; do
              sleep 0.2
            done
            
            TS="$(date +%s)"
            BACKUP="${'$'}{CURRENT_APP}.bak.${'$'}{TS}"
            
            # 尝试替换
            mv "${'$'}CURRENT_APP" "${'$'}BACKUP" || true
            /usr/bin/ditto "${'$'}NEW_APP" "${'$'}CURRENT_APP"
            
            # 清理解压产物（失败也无所谓）
            rm -rf "${'$'}NEW_APP" || true
            
            open "${'$'}CURRENT_APP" || true
        """.trimIndent()
        Files.writeString(script, content)
        script.toFile().setExecutable(true)
        return script
    }
}
