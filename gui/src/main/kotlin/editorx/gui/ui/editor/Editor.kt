package editorx.gui.ui.editor

import editorx.gui.ui.MainWindow
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.nio.file.Files
import javax.swing.*

class Editor(private val mainWindow: MainWindow) : JPanel() {
    private val fileToTab = mutableMapOf<File, Int>()
    private val tabToFile = mutableMapOf<Int, File>()
    private val tabbedPane = JTabbedPane()

    init {
        // 设置JPanel的布局
        layout = java.awt.BorderLayout()
        
        // 配置内部的JTabbedPane
        tabbedPane.apply {
            tabPlacement = JTabbedPane.TOP
            tabLayoutPolicy = JTabbedPane.WRAP_TAB_LAYOUT
            
            // 设置标签页左对齐 - 使用自定义UI
            setUI(object : javax.swing.plaf.basic.BasicTabbedPaneUI() {
                override fun getTabRunCount(tabPane: javax.swing.JTabbedPane): Int {
                    return 1 // 强制单行显示
                }
                
                override fun getTabRunOverlay(placement: Int): Int {
                    return 0 // 无重叠
                }
                
                override fun getTabInsets(placement: Int, tabIndex: Int): java.awt.Insets {
                    return java.awt.Insets(3, 6, 3, 6) // 调整标签页内边距
                }
                
                override fun getTabAreaInsets(placement: Int): java.awt.Insets {
                    return java.awt.Insets(0, 0, 0, 0) // 移除标签区域边距
                }
            })
        }
        
        // 将JTabbedPane添加到JPanel中
        add(tabbedPane, java.awt.BorderLayout.CENTER)
        
        // 设置拖放支持 - 确保整个Editor区域都支持拖放
        enableDropTarget()
        
        // 设置TransferHandler来处理拖放
        transferHandler = object : javax.swing.TransferHandler() {
            override fun canImport(support: javax.swing.TransferHandler.TransferSupport): Boolean {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor)
            }
            
            override fun importData(support: javax.swing.TransferHandler.TransferSupport): Boolean {
                if (!canImport(support)) {
                    return false
                }
                
                try {
                    val transferable = support.transferable
                    val dataFlavor = java.awt.datatransfer.DataFlavor.javaFileListFlavor
                    
                    if (transferable.isDataFlavorSupported(dataFlavor)) {
                        @Suppress("UNCHECKED_CAST")
                        val fileList = transferable.getTransferData(dataFlavor) as List<File>
                        
                        // 打开所有拖入的文件
                        for (file in fileList) {
                            if (file.isFile && file.canRead()) {
                                openFile(file)
                            }
                        }
                        
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                return false
            }
        }
    }

    fun openFile(file: File) {
        if (fileToTab.containsKey(file)) { 
            tabbedPane.selectedIndex = fileToTab[file]!!
            return 
        }
        val textArea = RSyntaxTextArea().apply {
            syntaxEditingStyle = detectSyntax(file)
            font = Font("Consolas", Font.PLAIN, 14)
        }
        try {
            textArea.text = Files.readString(file.toPath())
            textArea.discardAllEdits()
        } catch (e: Exception) {
            textArea.text = "无法读取文件: ${e.message}"
            textArea.isEditable = false
        }
        val scroll = RTextScrollPane(textArea)
        val title = file.name
        tabbedPane.addTab(title, null, scroll, null)
        val index = tabbedPane.tabCount - 1
        fileToTab[file] = index
        tabToFile[index] = file
        val closeButton = createCloseButton(file, index)
        tabbedPane.setTabComponentAt(index, closeButton)
        tabbedPane.selectedIndex = index
    }

    private fun createCloseButton(file: File, index: Int): JPanel = JPanel().apply {
        layout = java.awt.BorderLayout(); isOpaque = false
        val label = JLabel(file.name).apply { 
            border = BorderFactory.createEmptyBorder(0, 0, 0, 5)
            horizontalAlignment = JLabel.LEFT
        }
        add(label, java.awt.BorderLayout.CENTER)
        val closeLabel = JLabel("×").apply {
            font = font.deriveFont(Font.BOLD, 14f)
            foreground = Color.GRAY
            preferredSize = Dimension(16, 16)
            horizontalAlignment = JLabel.CENTER
            verticalAlignment = JLabel.CENTER
            addMouseListener(object : MouseAdapter() {
                override fun mouseEntered(e: MouseEvent) { foreground = Color.RED; cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR) }
                override fun mouseExited(e: MouseEvent) { foreground = Color.GRAY; cursor = java.awt.Cursor.getDefaultCursor() }
                override fun mouseClicked(e: MouseEvent) { closeTab(index) }
            })
        }
        add(closeLabel, java.awt.BorderLayout.EAST)
    }

    private fun closeTab(index: Int) {
        if (index >= 0 && index < tabbedPane.tabCount) {
            val file = tabToFile[index]
            tabbedPane.removeTabAt(index)
            file?.let { fileToTab.remove(it) }
            tabToFile.remove(index)
            val newTabToFile = mutableMapOf<Int, File>()
            val newFileToTab = mutableMapOf<File, Int>()
            for (i in 0 until tabbedPane.tabCount) {
                val f = tabToFile[i]
                if (f != null) { newTabToFile[i] = f; newFileToTab[f] = i }
            }
            tabToFile.clear(); tabToFile.putAll(newTabToFile)
            fileToTab.clear(); fileToTab.putAll(newFileToTab)
        }
    }

    private fun detectSyntax(file: File): String = when {
        file.name.endsWith(".smali") -> "text/smali"
        file.name.endsWith(".xml") -> SyntaxConstants.SYNTAX_STYLE_XML
        file.name.endsWith(".java") -> SyntaxConstants.SYNTAX_STYLE_JAVA
        file.name.endsWith(".kt") -> SyntaxConstants.SYNTAX_STYLE_KOTLIN
        file.name.endsWith(".js") -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
        file.name.endsWith(".css") -> SyntaxConstants.SYNTAX_STYLE_CSS
        file.name.endsWith(".html") -> SyntaxConstants.SYNTAX_STYLE_HTML
        file.name.endsWith(".json") -> SyntaxConstants.SYNTAX_STYLE_JSON
        else -> SyntaxConstants.SYNTAX_STYLE_NONE
    }

    fun getCurrentFile(): File? = if (tabbedPane.selectedIndex >= 0) tabToFile[tabbedPane.selectedIndex] else null
    fun hasUnsavedChanges(): Boolean = false

    private fun enableDropTarget() {
        // 为整个Editor组件设置拖放支持
        DropTarget(this, object : DropTargetListener {
            override fun dragEnter(dtde: DropTargetDragEvent) {
                if (isDragAcceptable(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY)
                } else {
                    dtde.rejectDrag()
                }
            }

            override fun dragOver(dtde: DropTargetDragEvent) {
                if (isDragAcceptable(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY)
                } else {
                    dtde.rejectDrag()
                }
            }

            override fun dropActionChanged(dtde: DropTargetDragEvent) {
                if (isDragAcceptable(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY)
                } else {
                    dtde.rejectDrag()
                }
            }

            override fun dragExit(dtde: DropTargetEvent) {
                // 拖放退出，无需特殊处理
            }

            override fun drop(dtde: DropTargetDropEvent) {
                if (!isDragAcceptable(dtde)) {
                    dtde.rejectDrop()
                    return
                }

                dtde.acceptDrop(DnDConstants.ACTION_COPY)

                try {
                    val transferable = dtde.transferable
                    val dataFlavors = transferable.transferDataFlavors

                    for (dataFlavor in dataFlavors) {
                        if (dataFlavor.isFlavorJavaFileListType) {
                            @Suppress("UNCHECKED_CAST")
                            val fileList = transferable.getTransferData(dataFlavor) as List<File>

                            for (file in fileList) {
                                if (file.isFile && file.canRead()) {
                                    openFile(file)
                                }
                            }

                            dtde.dropComplete(true)
                            return
                        }
                    }

                    dtde.dropComplete(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    dtde.dropComplete(false)
                }
            }
        })
    }

    private fun isDragAcceptable(event: DropTargetDragEvent): Boolean {
        val transferable = event.transferable
        val dataFlavors = transferable.transferDataFlavors

        for (dataFlavor in dataFlavors) {
            if (dataFlavor.isFlavorJavaFileListType) {
                return true
            }
        }
        return false
    }

    private fun isDragAcceptable(event: DropTargetDropEvent): Boolean {
        val transferable = event.transferable
        val dataFlavors = transferable.transferDataFlavors

        for (dataFlavor in dataFlavors) {
            if (dataFlavor.isFlavorJavaFileListType) {
                return true
            }
        }
        return false
    }
}
