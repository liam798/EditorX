package editorx.gui.update

import editorx.core.util.SystemUtils
import editorx.gui.MainWindow
import org.slf4j.LoggerFactory
import java.io.OutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

object UpdateManager {
    private val logger = LoggerFactory.getLogger(UpdateManager::class.java)

    private const val REPO = "Valiant-Cat/EditorX"

    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val releaseName: String?,
        val releaseNotes: String?,
        val releasePageUrl: String?,
        val assetName: String,
        val downloadUrl: String,
    )

    private val started = AtomicBoolean(false)
    private val http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

    fun checkUpdate(mainWindow: MainWindow) {
        if (!started.compareAndSet(false, true)) return
        Thread {
            checkUpdateInternal(mainWindow)
        }.apply {
            isDaemon = true
            name = "UpdateChecker"
            start()
        }
    }

    private fun checkUpdateInternal(mainWindow: MainWindow) {
        val current = VersionUtil.currentVersion()
        val latest = GitHubReleaseApi.fetchLatestRelease(REPO) ?: return

        val latestVersion = VersionUtil.normalizeTag(latest.tagName)
        val hasUpdate = VersionUtil.isNewer(current, latestVersion)
        if (!hasUpdate) return

        val asset = selectAsset(latest.assets) ?: run {
            logger.info("发现新版本，但未找到可下载资源：{}", latest.assets.map { it.name })
            return
        }

        val info = UpdateAvailable(
            currentVersion = current,
            latestVersion = latestVersion,
            releaseName = latest.name,
            releaseNotes = latest.body,
            releasePageUrl = latest.htmlUrl,
            assetName = asset.name,
            downloadUrl = asset.browserDownloadUrl,
        )

        SwingUtilities.invokeLater {
            mainWindow.statusBar.setUpdateHint("有更新：$latestVersion") {
                onUpdateClicked(mainWindow, info)
            }
        }
    }

    private fun selectAsset(assets: List<GitHubReleaseApi.Asset>): GitHubReleaseApi.Asset? {
        val mac = SystemUtils.isMacOS()
        if (mac) {
            assets.firstOrNull { it.name.endsWith(".app.zip") }?.let { return it }
            assets.firstOrNull { it.name.contains("mac", ignoreCase = true) && it.name.endsWith(".zip") }?.let { return it }
        }
        // 兜底：优先找 gui.zip
        assets.firstOrNull { it.name == "gui.zip" }?.let { return it }
        assets.firstOrNull { it.name.endsWith(".zip") }?.let { return it }
        return null
    }

    private fun onUpdateClicked(mainWindow: MainWindow, info: UpdateAvailable) {
        val confirm = UpdateDialog.confirmUpdate(mainWindow, info)
        if (!confirm) return
        downloadAndUpdate(mainWindow, info)
    }

    private fun downloadAndUpdate(mainWindow: MainWindow, info: UpdateAvailable) {
        val cancelled = AtomicBoolean(false)
        val dialog = UpdateDialog.showDownloadingDialog(mainWindow, "正在更新")
        dialog.setOnCancel { cancelled.set(true) }
        dialog.show()

        val handle = mainWindow.statusBar.beginProgressTask(
            message = "下载更新…",
            indeterminate = true,
            cancellable = true,
            onCancel = { cancelled.set(true) },
            maximum = 100
        )

        Thread {
            val zipFile = runCatching { downloadToCache(mainWindow, info, handle, dialog, cancelled) }.getOrNull()
            SwingUtilities.invokeLater {
                mainWindow.statusBar.endProgressTask(handle)
                dialog.close()
            }

            if (cancelled.get()) {
                logger.info("更新下载已取消")
                return@Thread
            }
            if (zipFile == null) {
                SwingUtilities.invokeLater {
                    JOptionPane.showMessageDialog(
                        mainWindow,
                        "下载更新失败，请稍后重试。",
                        "更新失败",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
                return@Thread
            }

            val apply = SelfUpdater.applyDownloadedPackage(zipFile, ProcessHandle.current().pid())
            if (!apply.success) {
                SwingUtilities.invokeLater {
                    JOptionPane.showMessageDialog(
                        mainWindow,
                        apply.message,
                        "更新失败",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
                return@Thread
            }

            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(
                    mainWindow,
                    "下载完成，正在更新并重启…",
                    "更新",
                    JOptionPane.INFORMATION_MESSAGE
                )
                // 退出让外部脚本接管替换
                System.exit(0)
            }
        }.apply {
            isDaemon = true
            name = "Updater"
            start()
        }
    }

    private fun downloadToCache(
        mainWindow: MainWindow,
        info: UpdateAvailable,
        handle: editorx.gui.workbench.statusbar.StatusBar.ProgressHandle,
        dialog: UpdateDialog.DownloadingDialog,
        cancelled: AtomicBoolean
    ): Path? {
        val dir = VersionUtil.ensureUpdateCacheDir()
        val file = dir.resolve("EditorX-${info.latestVersion}-${info.assetName}")

        val request = HttpRequest.newBuilder()
            .uri(URI.create(info.downloadUrl))
            .header("User-Agent", "EditorX")
            .GET()
            .build()

        val resp = http.send(request, HttpResponse.BodyHandlers.ofInputStream())
        if (resp.statusCode() !in 200..299) {
            logger.info("下载更新返回非 2xx：status={}", resp.statusCode())
            return null
        }

        val total = resp.headers().firstValue("Content-Length").orElse(null)?.toLongOrNull()
        val start = Instant.now()

        resp.body().use { input ->
            Files.newOutputStream(file).use { out ->
                copyWithProgress(input, out, total, cancelled) { done, percent ->
                    SwingUtilities.invokeLater {
                        if (percent == null) {
                            val msg = "下载更新… ${formatBytes(done)}"
                            mainWindow.statusBar.updateProgressTask(handle, msg, indeterminate = true)
                            dialog.update(msg, null)
                        } else {
                            val msg = "下载更新…"
                            mainWindow.statusBar.updateProgressTask(handle, percent, msg)
                            dialog.update("$msg ${formatBytes(done)}", percent)
                        }
                    }
                }
            }
        }

        if (cancelled.get()) {
            runCatching { Files.deleteIfExists(file) }
            return null
        }

        logger.info("更新包下载完成：{}（耗时={}s）", file, java.time.Duration.between(start, Instant.now()).seconds)
        return file
    }

    private fun copyWithProgress(
        input: java.io.InputStream,
        out: OutputStream,
        total: Long?,
        cancelled: AtomicBoolean,
        onProgress: (doneBytes: Long, percent: Int?) -> Unit
    ) {
        val buf = ByteArray(8192)
        var done = 0L
        var lastNotify = 0L
        while (true) {
            if (cancelled.get()) break
            val r = input.read(buf)
            if (r < 0) break
            out.write(buf, 0, r)
            done += r
            if (done - lastNotify > 128 * 1024) {
                lastNotify = done
                val percent = if (total != null && total > 0) ((done * 100) / total).toInt().coerceIn(0, 100) else null
                onProgress(done, percent)
            }
        }
        val percent = if (total != null && total > 0) ((done * 100) / total).toInt().coerceIn(0, 100) else null
        onProgress(done, percent)
    }

    private fun formatBytes(bytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024.0
        val gb = mb * 1024.0
        val b = bytes.toDouble()
        return when {
            b >= gb -> String.format("%.2f GB", b / gb)
            b >= mb -> String.format("%.2f MB", b / mb)
            b >= kb -> String.format("%.2f KB", b / kb)
            else -> "${bytes} B"
        }
    }
}
