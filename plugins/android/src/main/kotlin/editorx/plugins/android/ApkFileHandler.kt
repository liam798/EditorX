package editorx.plugins.android

import editorx.core.external.ApkTool
import editorx.core.external.Jadx
import editorx.core.i18n.I18n
import editorx.core.i18n.I18nKeys
import editorx.core.plugin.FileHandler
import editorx.core.gui.GuiExtension
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * APK 文件处理器
 * 负责处理 APK 文件的打开、反编译等功能
 */
class ApkFileHandler(private val gui: GuiExtension) : FileHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(ApkFileHandler::class.java)
        private const val JADX_OUTPUT_DIR_NAME = ".jadx"
    }

    override fun canHandle(file: File): Boolean {
        return file.isFile && file.extension.lowercase() == "apk"
    }

    override fun handleOpenFile(file: File): Boolean {
        // 同步等待用户选择，避免异步操作导致的循环调用问题
        var result: Int = JOptionPane.CLOSED_OPTION
        if (SwingUtilities.isEventDispatchThread()) {
            // 如果已经在 EDT 线程，直接显示对话框
            result = JOptionPane.showConfirmDialog(
                null,
                I18n.translate(I18nKeys.Dialog.DETECTED_APK),
                I18n.translate(I18nKeys.Dialog.OPEN_APK_FILE),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            )
        } else {
            // 如果不在 EDT 线程，使用 invokeAndWait 同步等待
            SwingUtilities.invokeAndWait {
                result = JOptionPane.showConfirmDialog(
                    null,
                    I18n.translate(I18nKeys.Dialog.DETECTED_APK),
                    I18n.translate(I18nKeys.Dialog.OPEN_APK_FILE),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                )
            }
        }

        when (result) {
            JOptionPane.YES_OPTION -> {
                // 转为项目：反编译 APK
                handleApkFileConversion(file)
                return true // 已处理，不再继续
            }
            JOptionPane.NO_OPTION -> {
                // 直接打开文件，返回 false 让 Editor 的默认逻辑处理
                // 由于 FileHandlerRegistry 会跟踪正在处理的文件，不会再次触发处理器
                return false
            }
            else -> {
                // 用户取消或关闭对话框：什么都不做
                return true // 已处理（用户取消了），不再继续
            }
        }
    }

    /**
     * 处理 APK 文件转换（反编译）
     */
    private fun handleApkFileConversion(apkFile: File) {
        // 在后台线程中处理APK反编译
        Thread {
            try {
                val outputDir = File(apkFile.parentFile, apkFile.nameWithoutExtension + "_decompiled")
                val jadxOutputDir = File(outputDir, JADX_OUTPUT_DIR_NAME)
                val jadxStagingDir = File(apkFile.parentFile, outputDir.name + "_jadx")
                
                // 检查输出目录是否已存在
                if (outputDir.exists()) {
                    val options = arrayOf("打开已存在的项目", "重新反编译")
                    var choice: Int = -1
                    SwingUtilities.invokeAndWait {
                        choice = JOptionPane.showOptionDialog(
                            null,
                            "此Apk对应的反编译目录 \"${outputDir.name}\" 已存在。\n\n请选择操作：",
                            "目录已存在",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0] // 默认选择"打开已存在的项目"
                        )
                    }

                    when (choice) {
                        JOptionPane.YES_OPTION -> {
                            // 直接打开已存在的项目
                            SwingUtilities.invokeLater {
                                gui.openWorkspace(outputDir)
                            }
                            // 若缺少 JADX 输出，则后台补全生成
                            if (!jadxOutputDir.exists()) {
                                Thread {
                                    try {
                                        SwingUtilities.invokeLater {
                                            gui.showProgress("正在生成 Java 源码（Jadx）…", indeterminate = true)
                                        }
                                        val jadxResult = Jadx.decompile(apkFile, jadxOutputDir)
                                        SwingUtilities.invokeLater {
                                            gui.hideProgress()
                                            val errorCount = jadxResult.errorCount ?: 0
                                            when {
                                                errorCount > 0 -> {
                                                    val logFile = File(jadxOutputDir, "jadx.log")
                                                    JOptionPane.showMessageDialog(
                                                        null,
                                                        "Jadx 反编译完成，但存在 $errorCount 个错误（Java 源码可能不完整）。\n日志：${logFile.absolutePath}",
                                                        "提示",
                                                        JOptionPane.WARNING_MESSAGE
                                                    )
                                                }

                                                jadxResult.status != Jadx.Status.SUCCESS -> {
                                                    val logFile = File(jadxOutputDir, "jadx.log")
                                                    val hint = if (logFile.exists()) "\n日志：${logFile.absolutePath}" else ""
                                                    JOptionPane.showMessageDialog(
                                                        null,
                                                        "Jadx 反编译失败（exitCode=${jadxResult.exitCode}）。$hint",
                                                        "提示",
                                                        JOptionPane.WARNING_MESSAGE
                                                    )
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        SwingUtilities.invokeLater {
                                            gui.hideProgress()
                                            JOptionPane.showMessageDialog(
                                                null,
                                                "Jadx 反编译失败: ${e.message}",
                                                "错误",
                                                JOptionPane.ERROR_MESSAGE
                                            )
                                        }
                                    }
                                }.start()
                            }
                            return@Thread
                        }
                        JOptionPane.NO_OPTION -> {
                            // 重新反编译，继续执行后续逻辑
                        }
                        else -> {
                            // 用户取消或关闭对话框
                            return@Thread
                        }
                    }
                }

                // 显示进度
                SwingUtilities.invokeLater {
                    gui.showProgress("正在反编译 APK（apktool + jadx）…", indeterminate = true)
                }

                // 删除已存在的输出目录
                if (outputDir.exists()) {
                    deleteRecursively(outputDir)
                }
                if (jadxStagingDir.exists()) {
                    deleteRecursively(jadxStagingDir)
                }

                // 同时执行 apktool + jadx 反编译
                var apktoolResult: ApkTool.RunResult? = null
                var jadxResult: Jadx.RunResult? = null
                val cancelJadx = java.util.concurrent.atomic.AtomicBoolean(false)

                val apktoolThread = Thread {
                    apktoolResult = ApkTool.decompile(apkFile, outputDir, force = true) { false }
                }
                val jadxThread = Thread {
                    // 注意：避免在 apktool 运行时创建 outputDir（apktool 可能会强制覆盖目录）
                    // 先输出到 staging 目录，待 apktool 完成后再移动到 outputDir/.jadx
                    jadxResult = Jadx.decompile(apkFile, jadxStagingDir) { cancelJadx.get() }
                }

                apktoolThread.start()
                jadxThread.start()
                apktoolThread.join()

                val result = apktoolResult ?: ApkTool.RunResult(ApkTool.Status.FAILED, -1, "apktool 未返回结果")
                when (result.status) {
                    ApkTool.Status.SUCCESS -> {
                        // 从 APK 中提取 DEX 文件到输出目录
                        val originalDir = File(outputDir, "original")
                        extractDexFilesFromApk(apkFile, originalDir)

                        SwingUtilities.invokeLater {
                            gui.openWorkspace(outputDir)
                        }

                        // 等待 JADX 完成（用于全局搜索/资源树 Java 视图）
                        jadxThread.join()
                        val jResult = jadxResult
                        var moveError: Throwable? = null
                        // 若成功，移动到 outputDir/.jadx
                        if (jResult?.status == Jadx.Status.SUCCESS) {
                            runCatching {
                                if (jadxOutputDir.exists()) {
                                    deleteRecursively(jadxOutputDir)
                                }
                                if (jadxStagingDir.exists()) {
                                    jadxStagingDir.parentFile?.mkdirs()
                                    java.nio.file.Files.move(
                                        jadxStagingDir.toPath(),
                                        jadxOutputDir.toPath(),
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                    )
                                }
                            }.onFailure { moveError = it }
                        } else {
                            // 清理 staging，避免残留大目录
                            runCatching { if (jadxStagingDir.exists()) deleteRecursively(jadxStagingDir) }
                        }
                        SwingUtilities.invokeLater {
                            gui.hideProgress()
                            if (moveError != null) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "移动 JADX 输出失败: ${moveError?.message ?: "未知错误"}",
                                    "提示",
                                    JOptionPane.WARNING_MESSAGE
                                )
                            }
                            if (jResult == null) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "Jadx 未返回结果",
                                    "提示",
                                    JOptionPane.WARNING_MESSAGE
                                )
                            } else {
                                val errorCount = jResult.errorCount ?: 0
                                when {
                                    errorCount > 0 -> {
                                        val logFile = File(jadxOutputDir, "jadx.log")
                                        JOptionPane.showMessageDialog(
                                            null,
                                            "Jadx 反编译完成，但存在 $errorCount 个错误（Java 源码可能不完整）。\n日志：${logFile.absolutePath}",
                                            "提示",
                                            JOptionPane.WARNING_MESSAGE
                                        )
                                    }

                                    jResult.status != Jadx.Status.SUCCESS -> {
                                        val logFile = File(jadxOutputDir, "jadx.log")
                                        val hint = if (logFile.exists()) "\n日志：${logFile.absolutePath}" else ""
                                        JOptionPane.showMessageDialog(
                                            null,
                                            "Jadx 反编译失败（exitCode=${jResult.exitCode}）。$hint",
                                            "提示",
                                            JOptionPane.WARNING_MESSAGE
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ApkTool.Status.CANCELLED -> {
                        SwingUtilities.invokeLater {
                            gui.hideProgress()
                        }
                    }
                    ApkTool.Status.NOT_FOUND -> {
                        // 取消 jadx（避免后台进程继续跑）
                        cancelJadx.set(true)
                        runCatching { jadxThread.join(2000) }
                        runCatching { if (jadxStagingDir.exists()) deleteRecursively(jadxStagingDir) }
                        SwingUtilities.invokeLater {
                            gui.hideProgress()
                            JOptionPane.showMessageDialog(
                                null,
                                "未找到apktool，请确保apktool已安装并在PATH中",
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                    ApkTool.Status.FAILED -> {
                        // 取消 jadx（避免后台进程继续跑）
                        cancelJadx.set(true)
                        runCatching { jadxThread.join(2000) }
                        runCatching { if (jadxStagingDir.exists()) deleteRecursively(jadxStagingDir) }
                        SwingUtilities.invokeLater {
                            gui.hideProgress()
                            JOptionPane.showMessageDialog(
                                null,
                                "apktool执行失败: ${result.output}",
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("反编译APK失败", e)
                SwingUtilities.invokeLater {
                    gui.hideProgress()
                    JOptionPane.showMessageDialog(
                        null,
                        "反编译失败: ${e.message}",
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }.start()
    }

    /**
     * 从 APK 文件中提取 DEX 文件到输出目录（用于实时 smali to java 反编译）
     */
    private fun extractDexFilesFromApk(apkFile: File, outputDir: File) {
        try {
            logger.debug("开始从 APK 提取 DEX 文件: ${apkFile.absolutePath} -> ${outputDir.absolutePath}")

            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val zipFile = ZipFile(apkFile)
            val dexPattern = """^classes\d*\.dex$""".toRegex()
            var extractedCount = 0
            val extractedFiles = mutableListOf<String>()

            zipFile.entries().asSequence().forEach { entry ->
                if (dexPattern.matches(entry.name)) {
                    val outputDexFile = File(outputDir, entry.name)
                    try {
                        zipFile.getInputStream(entry).use { input ->
                            FileOutputStream(outputDexFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        extractedCount++
                        extractedFiles.add(entry.name)
                        logger.debug("提取 DEX 文件: ${entry.name} -> ${outputDexFile.absolutePath}")
                    } catch (e: Exception) {
                        logger.warn("提取 DEX 文件失败: ${entry.name}", e)
                    }
                }
            }

            zipFile.close()

            if (extractedCount > 0) {
                logger.info("从 APK 成功提取了 $extractedCount 个 DEX 文件到: ${outputDir.absolutePath}")
                logger.debug("提取的文件: ${extractedFiles.joinToString(", ")}")
            } else {
                logger.warn("未找到任何 DEX 文件在 APK 中: ${apkFile.absolutePath}")
            }
        } catch (e: Exception) {
            logger.error("提取 DEX 文件失败: ${e.message}", e)
            // 不抛出异常，因为反编译本身已经成功
        }
    }

    /**
     * 递归删除目录
     */
    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        file.delete()
    }
}
