package editor.gui

import editor.gui.plugin.PluginContextFactory
import editor.gui.plugin.PluginManager
import editor.gui.ui.MainWindow
import javax.swing.SwingUtilities
import javax.swing.UIManager
import java.util.logging.Logger
import java.util.logging.Level

/**
 * Editor GUI 主入口点
 */
fun main() {
    Logger.getLogger("").level = Level.INFO

    Thread.setDefaultUncaughtExceptionHandler { _, exception ->
        Logger.getLogger("").severe("未捕获的异常: ${exception.message}")
        exception.printStackTrace()
    }

    SwingUtilities.invokeLater {
        try {
            initializeApplication()
            initializeMainWindow()
        } catch (e: Exception) {
            Logger.getLogger("").severe("应用程序启动失败: ${e.message}")
            e.printStackTrace()
            System.exit(1)
        }
    }
}

private fun initializeApplication() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        Logger.getLogger("").warning("无法设置系统外观: ${e.message}")
    }

    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "APK Editor")

    Logger.getLogger("").info("APK Editor 初始化完成")
}

private fun initializeMainWindow() {
    // 显示主窗口
    val mv = MainWindow()
    mv.isVisible = true

    // 加载插件
    try {
        val contextFactory = PluginContextFactory(mv)
        val pluginManager = PluginManager(contextFactory)
        pluginManager.loadPlugins()
        mv.statusBar.setMessage("插件系统已启动")
    } catch (e: Exception) {
        mv.statusBar.setMessage("插件加载失败: ${e.message}")
    }
}

