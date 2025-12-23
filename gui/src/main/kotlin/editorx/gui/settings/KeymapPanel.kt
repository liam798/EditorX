package editorx.gui.settings

import editorx.core.i18n.I18n
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.AbstractTableModel

/**
 * 展示默认快捷键（暂不提供编辑功能）。
 */
class KeymapPanel : JPanel(BorderLayout()) {

    private data class ShortcutItem(
        val actionZh: String,
        val actionEn: String,
        val shortcut: String,
        val descriptionZh: String,
        val descriptionEn: String,
    )

    private val shortcuts = listOf(
        ShortcutItem("查找", "Find", keyStroke(KeyEvent.VK_F), "聚焦顶部搜索栏", "Focus search bar"),
        ShortcutItem("替换", "Replace", keyStroke(KeyEvent.VK_R), "展开替换行", "Expand replace row"),
        ShortcutItem("全局搜索", "Global Search", keyStroke(KeyEvent.VK_F, InputEvent.SHIFT_DOWN_MASK), "打开侧边栏搜索", "Open sidebar search"),
        ShortcutItem("打开文件", "Open File", keyStroke(KeyEvent.VK_O), "选择并打开文件", "Select and open a file"),
        ShortcutItem("保存文件", "Save File", keyStroke(KeyEvent.VK_S), "保存当前编辑内容", "Save current content"),
        ShortcutItem("关闭标签页", "Close Tab", keyStroke(KeyEvent.VK_W), "关闭当前标签页", "Close active tab"),
        ShortcutItem("切换侧边栏", "Toggle Sidebar", keyStroke(KeyEvent.VK_B), "显示或隐藏侧边栏", "Show or hide sidebar"),
        ShortcutItem("语言菜单", "Language Menu", keyStroke(KeyEvent.VK_L), "打开语言菜单", "Open language menu"),
    )

    private val tableModel = object : AbstractTableModel() {
        override fun getRowCount(): Int = shortcuts.size
        override fun getColumnCount(): Int = 3
        override fun getColumnName(column: Int): String = when (column) {
            0 -> if (isEnglish()) "Action" else "功能"
            1 -> "Shortcut"
            else -> if (isEnglish()) "Description" else "说明"
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val item = shortcuts[rowIndex]
            val english = isEnglish()
            return when (columnIndex) {
                0 -> if (english) item.actionEn else item.actionZh
                1 -> item.shortcut
                else -> if (english) item.descriptionEn else item.descriptionZh
            }
        }
    }

    private val table = JTable(tableModel).apply {
        rowHeight = 28
        setShowGrid(false)
        tableHeader.reorderingAllowed = false
    }

    init {
        border = BorderFactory.createEmptyBorder(16, 16, 16, 16)

        val header = JLabel(if (isEnglish()) "Keymap (Planned)" else "快捷键（规划中）").apply {
            font = font.deriveFont(Font.BOLD, 16f)
            border = BorderFactory.createEmptyBorder(0, 0, 12, 0)
        }

        val hintText = if (isEnglish()) {
            "Current list shows default shortcuts. Customization/export is under planning."
        } else {
            "当前列表展示默认快捷键，自定义与导出功能规划中。"
        }
        val hint = JLabel("<html>$hintText</html>", SwingConstants.LEFT).apply {
            border = BorderFactory.createEmptyBorder(0, 0, 12, 0)
        }

        val buttonBar = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            add(JButton(if (isEnglish()) "Add Note…" else "添加备注…").apply {
                isEnabled = false
                toolTipText = if (isEnglish()) "Shortcut customization is under development" else "快捷键自定义功能开发中"
            })
            add(JButton(if (isEnglish()) "Export…" else "导出配置…").apply {
                isEnabled = false
                toolTipText = if (isEnglish()) "Feature under development" else "功能开发中"
            })
        }

        add(header, BorderLayout.NORTH)
        add(JScrollPane(table).apply { border = BorderFactory.createEmptyBorder() }, BorderLayout.CENTER)
        add(
            JPanel(BorderLayout()).apply {
                border = BorderFactory.createEmptyBorder(12, 0, 0, 0)
                add(hint, BorderLayout.NORTH)
                add(buttonBar, BorderLayout.CENTER)
            },
            BorderLayout.SOUTH
        )
    }

    fun refresh() {
        tableModel.fireTableDataChanged()
    }

    private fun isEnglish(): Boolean = I18n.locale().language == java.util.Locale.ENGLISH.language

    companion object {
        private fun keyStroke(keyCode: Int, modifiers: Int = 0): String {
            val mask = java.awt.Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx
            val combo = mask or modifiers
            val modText = KeyEvent.getModifiersExText(combo)
            val keyText = KeyEvent.getKeyText(keyCode)
            return "$modText+$keyText"
        }
    }
}
