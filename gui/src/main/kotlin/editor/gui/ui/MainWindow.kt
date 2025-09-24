package editor.gui.ui

import editor.gui.plugin.PluginManager
import editor.gui.ui.activitybar.ActivityBar
import editor.gui.ui.editor.Editor
import editor.gui.ui.panel.Panel
import editor.gui.ui.sidebar.SideBar
import editor.gui.ui.statusbar.StatusBar
import editor.gui.ui.titlebar.TitleBar
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JSplitPane

/**
 * APK 编辑器主窗口
 * - 解耦 UI 构建与插件加载
 * - 提供显式的 showWindow()/loadPluginsSafely()
 */
object MainWindow : JFrame() {
    val instance: MainWindow get() = this

    // UI 组件
    val titleBar by lazy { TitleBar(this) }
    val activityBar by lazy { ActivityBar(this) }
    val sideBar by lazy { SideBar(this) }
    val editor by lazy { Editor(this) }
    val panel by lazy { Panel(this) }
    val statusBar by lazy { StatusBar(this) }

    private val horizontalSplit by lazy {
        JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, editor).apply {
            dividerLocation = 250
            isOneTouchExpandable = false
            dividerSize = 8
        }
    }

    private val verticalSplit by lazy {
        JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, panel).apply {
            dividerLocation = 700
            isOneTouchExpandable = false
            dividerSize = 8
        }
    }

    private var initialized = false

    private fun initIfNeeded() {
        if (initialized) return
        // 窗口属性
        title = "APK Editor"
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        size = Dimension(1400, 900)
        setLocationRelativeTo(null)
        minimumSize = Dimension(800, 600)

        // 布局
        layout = BorderLayout()
        add(titleBar, BorderLayout.NORTH)
        add(activityBar, BorderLayout.WEST)
        add(verticalSplit, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        // 组件连接
        activityBar.sideBar = sideBar
        activityBar.panel = panel

        initialized = true
    }

    /** 显示窗口（只构建一次） */
    fun init() {
        initIfNeeded()
        if (!isVisible) {
            isVisible = true
        }
    }

    /** 安全加载插件，并在状态栏反馈 */
    fun loadPluginsSafely() {
        try {
            PluginManager.loadPlugins()
            statusBar.setMessage("插件系统已启动")
        } catch (e: Exception) {
            statusBar.setMessage("插件加载失败: ${e.message}")
        }
    }
}
