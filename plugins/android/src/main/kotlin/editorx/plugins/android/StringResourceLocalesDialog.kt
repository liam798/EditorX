package editorx.plugins.android

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.File
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel

data class StringResourceLocalesResult(
    val key: String,
    val defaultValue: String,
    val overridesByValuesDir: Map<String, String>?,
    val syncAllExistingLocalesToDefault: Boolean,
    val removedValuesDirs: Set<String>,
)

object StringResourceLocalesDialog {
    fun show(
        workspaceRoot: File,
        title: String,
        initialKey: String,
        initialDefaultValue: String?,
        hintHtml: String,
    ): StringResourceLocalesResult? {
        val keyField = JTextField(initialKey.trim().ifEmpty { "app_name" }).apply { preferredSize = Dimension(220, 26) }
        val defaultValueField = JTextField(initialDefaultValue?.trim().orEmpty()).apply { preferredSize = Dimension(360, 26) }
        val syncAll = JCheckBox("一键同步：将所有已存在语言都设置为默认名称", false)

        // 当 key 变化时，默认名称需要跟随更新；当用户手动编辑默认名称后，不再自动覆盖。
        var userEditedDefault = false
        var lastAutoFilledKey: String? = null
        var programmaticDefaultUpdate = false

        val removedDirs = linkedSetOf<String>()
        var currentExistingDirs = emptySet<String>()
        defaultValueField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = onChange()
            override fun removeUpdate(e: DocumentEvent?) = onChange()
            override fun changedUpdate(e: DocumentEvent?) = onChange()
            private fun onChange() {
                if (programmaticDefaultUpdate) return
                val currentKey = keyField.text.trim().ifEmpty { "app_name" }
                if (lastAutoFilledKey == currentKey) {
                    // 仍是同一个 key 下用户修改，标记为手动编辑
                    userEditedDefault = true
                }
            }
        })

        val tableModel = object : DefaultTableModel(arrayOf("values* 目录", "翻译"), 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean = column == 1
        }
        val table = JTable(tableModel).apply {
            rowHeight = 22
            autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            columnModel.getColumn(0).preferredWidth = 160
            columnModel.getColumn(0).minWidth = 140
            selectionModel.selectionMode = javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        }

        val removeSelectedButton = JButton("移除所选").apply {
            isEnabled = false
            addActionListener {
                val selected = table.selectedRows.sortedDescending()
                if (selected.isEmpty()) return@addActionListener
                for (row in selected) {
                    val dir = (tableModel.getValueAt(row, 0) as? String)?.trim().orEmpty()
                    if (dir.isNotEmpty() && dir in currentExistingDirs) {
                        removedDirs.add(dir)
                    }
                    tableModel.removeRow(row)
                }
                isEnabled = table.selectedRowCount > 0
            }
        }
        table.selectionModel.addListSelectionListener {
            removeSelectedButton.isEnabled = table.selectedRowCount > 0
        }

        fun reloadTable(forceUpdateDefault: Boolean) {
            val key = keyField.text.trim().ifEmpty { "app_name" }
            val existing = AndroidAppInfoEditor.listStringValuesForKey(workspaceRoot, key)
            currentExistingDirs = existing.map { it.valuesDir }.toSet()
            removedDirs.clear()

            val defaultExisting = existing.firstOrNull { it.valuesDir == "values" }?.value
            if (forceUpdateDefault || (!userEditedDefault && lastAutoFilledKey != key)) {
                programmaticDefaultUpdate = true
                try {
                    defaultValueField.text = defaultExisting ?: ""
                    userEditedDefault = false
                    lastAutoFilledKey = key
                } finally {
                    programmaticDefaultUpdate = false
                }
            } else if (!defaultExisting.isNullOrBlank() && defaultValueField.text.trim().isEmpty()) {
                // 兼容：初次打开时如果没有默认值且未手动编辑，补一次
                programmaticDefaultUpdate = true
                try {
                    defaultValueField.text = defaultExisting
                    lastAutoFilledKey = key
                } finally {
                    programmaticDefaultUpdate = false
                }
            }

            tableModel.setRowCount(0)
            existing
                .filter { it.valuesDir != "values" }
                .sortedBy { it.valuesDir }
                .forEach { entry ->
                    tableModel.addRow(arrayOf(entry.valuesDir, entry.value ?: ""))
                }
        }
        reloadTable(forceUpdateDefault = false)

        keyField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = onChange()
            override fun removeUpdate(e: DocumentEvent?) = onChange()
            override fun changedUpdate(e: DocumentEvent?) = onChange()
            private fun onChange() {
                // key 改变：默认名称应随之刷新（覆盖为该 key 在 values 中的现有值或空）
                reloadTable(forceUpdateDefault = true)
            }
        })

        val addLocaleButton = JButton("添加语言…").apply {
            addActionListener {
                val dir = JOptionPane.showInputDialog(
                    null,
                    "请输入 values 目录名（例如 values-en、values-zh-rCN）",
                    "添加语言",
                    JOptionPane.PLAIN_MESSAGE
                ) ?: return@addActionListener
                val normalized = dir.trim()
                if (!normalized.startsWith("values") || normalized.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "目录名需以 values 开头", "提示", JOptionPane.WARNING_MESSAGE)
                    return@addActionListener
                }
                for (i in 0 until tableModel.rowCount) {
                    val existingDir = (tableModel.getValueAt(i, 0) as? String)?.trim()
                    if (existingDir == normalized) return@addActionListener
                }
                tableModel.addRow(arrayOf(normalized, ""))
            }
        }

        val content = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(10, 12, 10, 12)
            add(buildForm(keyField, defaultValueField, syncAll, table, addLocaleButton, removeSelectedButton), BorderLayout.CENTER)
            add(JLabel(hintHtml), BorderLayout.SOUTH)
        }

        val option = JOptionPane.showConfirmDialog(
            null,
            content,
            title,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        )
        if (option != JOptionPane.OK_OPTION) return null

        val key = keyField.text.trim().ifEmpty { "app_name" }
        val defaultValue = defaultValueField.text.trim()
        if (defaultValue.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请填写默认名称（values）", "提示", JOptionPane.WARNING_MESSAGE)
            return null
        }

        val overrides = if (syncAll.isSelected) {
            null
        } else {
            val map = linkedMapOf<String, String>()
            map["values"] = defaultValue
            for (i in 0 until tableModel.rowCount) {
                val dir = (tableModel.getValueAt(i, 0) as? String)?.trim().orEmpty()
                val value = (tableModel.getValueAt(i, 1) as? String)?.trim().orEmpty()
                if (dir.isNotEmpty() && value.isNotEmpty()) map[dir] = value
            }
            map
        }

        return StringResourceLocalesResult(
            key = key,
            defaultValue = defaultValue,
            overridesByValuesDir = overrides,
            syncAllExistingLocalesToDefault = syncAll.isSelected,
            removedValuesDirs = removedDirs.toSet(),
        )
    }

    private fun buildForm(
        keyField: JTextField,
        defaultNameField: JTextField,
        syncAll: JCheckBox,
        table: JTable,
        addLocaleButton: JButton,
        removeSelectedButton: JButton,
    ): JPanel {
        val grid = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            insets = Insets(6, 6, 6, 6)
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridx = 0
            gridy = 0
            anchor = GridBagConstraints.WEST
        }

        fun row(label: String, comp: java.awt.Component) {
            c.gridx = 0
            c.weightx = 0.0
            c.fill = GridBagConstraints.NONE
            grid.add(JLabel(label), c)

            c.gridx = 1
            c.weightx = 1.0
            c.fill = GridBagConstraints.HORIZONTAL
            grid.add(comp, c)
            c.gridy++
        }

        row("key（@string）", keyField)

        row("默认语言", defaultNameField)

        c.gridx = 1
        c.weightx = 1.0
        c.fill = GridBagConstraints.HORIZONTAL
        grid.add(syncAll, c)
        c.gridy++

        // 多语言配置：与其他字段保持一致的“左标题 + 右内容”布局
        val scroll = JScrollPane(table).apply { preferredSize = Dimension(520, 180) }
        val localesPanel = JPanel(BorderLayout()).apply {
            add(scroll, BorderLayout.CENTER)
            add(
                JPanel(java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 6)).apply {
                    isOpaque = false
                    add(addLocaleButton)
                    add(javax.swing.Box.createHorizontalStrut(8))
                    add(removeSelectedButton)
                },
                BorderLayout.SOUTH
            )
        }

        c.gridx = 0
        c.weightx = 0.0
        c.weighty = 0.0
        c.fill = GridBagConstraints.NONE
        grid.add(JLabel("多语言配置"), c)

        c.gridx = 1
        c.weightx = 1.0
        c.weighty = 1.0
        c.fill = GridBagConstraints.BOTH
        grid.add(localesPanel, c)
        c.gridy++
        c.weighty = 0.0
        c.fill = GridBagConstraints.HORIZONTAL

        return grid
    }
}
