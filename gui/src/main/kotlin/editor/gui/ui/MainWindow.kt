package editor.gui.ui

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
class MainWindow : JFrame() {

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

    init {
        setupWindow()
        setupLayout()
        connectComponents()
    }

    private fun setupWindow() {
        // 窗口属性
        title = "APK Editor"
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        size = Dimension(1400, 900)
        setLocationRelativeTo(null)
        minimumSize = Dimension(800, 600)
    }

    private fun setupLayout() {
        // 布局
        layout = BorderLayout()
        add(titleBar, BorderLayout.NORTH)
        add(activityBar, BorderLayout.WEST)
        add(verticalSplit, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)
    }

    private fun connectComponents() {
        // 组件连接
        activityBar.sideBar = sideBar
        activityBar.panel = panel
    }
}
