package editorx.gui

import editorx.core.gui.GuiContext
import editorx.core.util.SystemUtils
import editorx.gui.workbench.editor.Editor
import editorx.gui.workbench.explorer.Explorer
import editorx.gui.workbench.menubar.MenuBar
import editorx.gui.workbench.navigationbar.NavigationBar
import editorx.gui.workbench.sidebar.SideBar
import editorx.gui.workbench.statusbar.StatusBar
import editorx.gui.workbench.titlebar.TitleBar
import editorx.gui.workbench.toolbar.ToolBar
import editorx.gui.widget.NoLineSplitPaneUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseMotionAdapter
import java.io.File
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSplitPane

class MainWindow(val guiContext: GuiContext) : JFrame() {

    // UI 组件
    private val _menuBar by lazy { MenuBar(this) }
    val titleBar by lazy { TitleBar(this) }
    val toolBar by lazy { ToolBar(this) }
    val sideBar by lazy { SideBar(this) }
    val editor by lazy { Editor(this) }
    val statusBar by lazy { StatusBar(this) }
    val navigationBar by lazy { NavigationBar(this) }

    private val horizontalSplit by lazy {
        JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, editor).apply {
            dividerLocation = 0  // 初始时隐藏SideBar
            isOneTouchExpandable = false
            dividerSize = 0  // 初始时 SideBar 关闭，隐藏拖拽条
        }
    }

    init {
        initWindow()
        setupLayout()
    }

    private fun initWindow() {
        // 窗口属性
        title = "EditorX"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(1280, 800)
        setLocationRelativeTo(null)
        minimumSize = Dimension(800, 600)

        if (SystemUtils.isMacOS()) {
            // 启用 macOS 外观
            System.setProperty("apple.laf.useScreenMenuBar", "true")
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "EditorX")

            // 保留原生窗口装饰，但启用全窗口内容模式
            rootPane.putClientProperty("apple.awt.fullWindowContent", true)
            rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
            rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
        }
    }

    private fun setupLayout() {
        // 布局
        layout = BorderLayout()
        // 正确安装菜单栏，避免某些 LAF 下作为普通组件加入导致不显示
        jMenuBar = _menuBar

        // 将 titleBar 和 toolBar 放在一个垂直容器中
        val topContainer = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(titleBar, BorderLayout.NORTH)
            add(toolBar, BorderLayout.SOUTH)
        }
        add(topContainer, BorderLayout.NORTH)

        // 将 navigationBar 和 horizontalSplit 放在一个容器中
        val centerContainer = JPanel(BorderLayout()).apply {
            isOpaque = false
            add(navigationBar, BorderLayout.NORTH)
            add(horizontalSplit, BorderLayout.CENTER)
        }
        add(centerContainer, BorderLayout.CENTER)
        add(statusBar, BorderLayout.SOUTH)

        horizontalSplit.setUI(NoLineSplitPaneUI())
        horizontalSplit.border = javax.swing.BorderFactory.createEmptyBorder()
        // 启用连续布局，减少布局跳动
        horizontalSplit.isContinuousLayout = true

        // 当用户手动拖动分割条时，同步 SideBar 状态
        horizontalSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY) { _ ->
            val visible = horizontalSplit.dividerLocation > 0
            // 同步SideBar内部状态与分割条位置
            sideBar.syncVisibilityWithDivider()

            // 根据 SideBar 可见性调整拖拽条大小
            horizontalSplit.dividerSize = if (visible) 4 else 0

            // 同步更新ToolBar中的侧边栏toggle按钮
            titleBar.updateSideBarIcon(visible)
        }

        // 注册 Explorer，但不自动打开 SideBar（保持默认关闭状态）
        sideBar.showView("explorer", Explorer(this), autoShow = false)

        // 添加自定义拖拽功能：在 SideBar 右边缘和 Editor 左边缘检测拖拽
        addDragListeners()
    }

    private var isDragging = false
    private var dragStartX = 0
    private var dragStartLocation = 0

    private fun addDragListeners() {
        // 在 SideBar 右边缘添加鼠标监听器
        sideBar.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: java.awt.event.MouseEvent) {
                val rightEdge = sideBar.width
                val mouseX = e.x
                // 检测是否在右边缘 5 像素范围内
                if (mouseX >= rightEdge - 5 && mouseX <= rightEdge && horizontalSplit.dividerLocation > 0) {
                    isDragging = true
                    dragStartX = e.xOnScreen
                    dragStartLocation = horizontalSplit.dividerLocation
                    sideBar.cursor = java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR)
                }
            }

            override fun mouseReleased(e: java.awt.event.MouseEvent) {
                if (isDragging) {
                    isDragging = false
                    sideBar.cursor = java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR)
                    editor.cursor = java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR)
                }
            }
        })

        sideBar.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: java.awt.event.MouseEvent) {
                if (isDragging) {
                    val deltaX = e.xOnScreen - dragStartX
                    val newLocation = (dragStartLocation + deltaX).coerceAtLeast(0)
                        .coerceAtMost(horizontalSplit.width)
                    horizontalSplit.dividerLocation = newLocation
                }
            }

            override fun mouseMoved(e: java.awt.event.MouseEvent) {
                val rightEdge = sideBar.width
                val mouseX = e.x
                if (mouseX >= rightEdge - 5 && mouseX <= rightEdge && horizontalSplit.dividerLocation > 0) {
                    sideBar.cursor = java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR)
                } else {
                    sideBar.cursor = java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR)
                }
            }
        })

        // 在 Editor 左边缘添加鼠标监听器
        editor.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: java.awt.event.MouseEvent) {
                val mouseX = e.x
                // 检测是否在左边缘 5 像素范围内
                if (mouseX in 0..5 && horizontalSplit.dividerLocation > 0) {
                    isDragging = true
                    dragStartX = e.xOnScreen
                    dragStartLocation = horizontalSplit.dividerLocation
                    editor.cursor = java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR)
                }
            }

            override fun mouseReleased(e: java.awt.event.MouseEvent) {
                if (isDragging) {
                    isDragging = false
                    sideBar.cursor = java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR)
                    editor.cursor = java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR)
                }
            }
        })

        editor.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: java.awt.event.MouseEvent) {
                if (isDragging) {
                    val deltaX = e.xOnScreen - dragStartX
                    val newLocation = (dragStartLocation + deltaX).coerceAtLeast(0)
                        .coerceAtMost(horizontalSplit.width)
                    horizontalSplit.dividerLocation = newLocation
                }
            }

            override fun mouseMoved(e: java.awt.event.MouseEvent) {
                val mouseX = e.x
                if (mouseX in 0..5 && horizontalSplit.dividerLocation > 0) {
                    editor.cursor = java.awt.Cursor(java.awt.Cursor.E_RESIZE_CURSOR)
                } else {
                    editor.cursor = java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR)
                }
            }
        })
    }

    /**
     * 更新面包屑导航栏
     */
    fun updateNavigation(currentFile: File?) {
        navigationBar.update(currentFile)
    }
}
