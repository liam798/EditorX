package editorx.plugins.android

import editorx.core.gui.GuiExtension
import editorx.core.i18n.I18n
import editorx.core.i18n.I18nKeys
import org.slf4j.LoggerFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.ListSelectionModel
import javax.swing.JTable
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.AbstractTableModel

object AppIconDensitiesDialog {
    private val logger = LoggerFactory.getLogger(AppIconDensitiesDialog::class.java)

    private class IconCandidateTableModel(
        private var items: List<IconCandidate>
    ) : AbstractTableModel() {
        fun setItems(newItems: List<IconCandidate>) {
            items = newItems
            fireTableDataChanged()
        }

        fun getItem(row: Int): IconCandidate? = items.getOrNull(row)

        fun bestRowIndex(): Int? {
            if (items.isEmpty()) return null
            var best = 0
            var bestArea = -1
            for ((i, c) in items.withIndex()) {
                val area = (c.width ?: 0) * (c.height ?: 0)
                if (area > bestArea) {
                    bestArea = area
                    best = i
                }
            }
            return best
        }

        override fun getRowCount(): Int = items.size

        override fun getColumnCount(): Int = 3

        override fun getColumnName(column: Int): String {
            return when (column) {
                0 -> "目录"
                1 -> "文件"
                2 -> "尺寸"
                else -> ""
            }
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val c = items[rowIndex]
            return when (columnIndex) {
                0 -> c.resDir
                1 -> c.file.name
                2 -> if (c.width != null && c.height != null) "${c.width}x${c.height}" else "未知"
                else -> ""
            }
        }
    }

    fun show(gui: GuiExtension) {
        val workspaceRoot = gui.getWorkspaceRoot()
        if (workspaceRoot == null) {
            JOptionPane.showMessageDialog(
                null,
                I18n.translate(I18nKeys.ToolbarMessage.WORKSPACE_NOT_OPENED),
                I18n.translate(I18nKeys.Dialog.TIP),
                JOptionPane.INFORMATION_MESSAGE
            )
            return
        }

        val current = AndroidAppInfoEditor.readAppInfo(workspaceRoot)
        if (current == null) {
            JOptionPane.showMessageDialog(
                null,
                "未找到或无法读取 AndroidManifest.xml",
                I18n.translate(I18nKeys.Dialog.ERROR),
                JOptionPane.ERROR_MESSAGE
            )
            return
        }

        val iconRefField = JTextField(current.iconValue ?: "").apply { preferredSize = Dimension(360, 26) }
        val generateMultiDensity = JCheckBox("按密度生成多尺寸图标（mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi）", true)
        val createMissing = JCheckBox("创建缺失的密度目录/文件（res/mipmap-*/ 或 res/drawable-*）", false)

        val iconPreview = JLabel().apply {
            preferredSize = Dimension(72, 72)
            minimumSize = Dimension(72, 72)
            maximumSize = Dimension(72, 72)
            horizontalAlignment = JLabel.CENTER
            verticalAlignment = JLabel.CENTER
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(6, 6, 6, 6),
            )
        }

        val iconCandidatesModel = IconCandidateTableModel(emptyList())
        val iconCandidatesTable = JTable(iconCandidatesModel).apply {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            setRowSelectionAllowed(true)
            setColumnSelectionAllowed(false)
            rowHeight = 22
            autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
            setShowGrid(true)
        }
        val iconCandidatesScroll = JScrollPane(iconCandidatesTable).apply {
            preferredSize = Dimension(360, 140)
        }

        fun previewCandidate(candidate: IconCandidate?) {
            if (candidate == null) {
                iconPreview.icon = null
                iconPreview.text = "无预览"
                return
            }
            iconPreview.icon = null
            iconPreview.text = "无预览"
            setPreviewFromFile(iconPreview, candidate.file)
        }
        iconCandidatesTable.selectionModel.addListSelectionListener {
            if (it.valueIsAdjusting) return@addListSelectionListener
            previewCandidate(iconCandidatesModel.getItem(iconCandidatesTable.selectedRow))
        }

        fun refreshCurrentIconEcho() {
            val iconValue = iconRefField.text.trim().ifEmpty { current.iconValue?.trim().orEmpty() }
            val candidates = AndroidAppInfoEditor.resolveIconCandidates(workspaceRoot, iconValue)

            val directPreviewables = candidates.filter { it.width != null && it.height != null }
            val fallbackPreviewables = AndroidAppInfoEditor.resolveIconPreviewCandidates(workspaceRoot, iconValue)
            val previewables = if (directPreviewables.isNotEmpty()) directPreviewables else fallbackPreviewables

            iconCandidatesModel.setItems(previewables)
            val best = iconCandidatesModel.bestRowIndex()
            if (best != null) {
                iconCandidatesTable.setRowSelectionInterval(best, best)
            } else {
                previewCandidate(null)
            }
        }
        refreshCurrentIconEcho()
        iconRefField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = refreshCurrentIconEcho()
            override fun removeUpdate(e: DocumentEvent?) = refreshCurrentIconEcho()
            override fun changedUpdate(e: DocumentEvent?) = refreshCurrentIconEcho()
        })

        val pngPathField = JTextField("").apply {
            isEditable = false
            preferredSize = Dimension(280, 26)
        }
        var selectedPng: File? = null
        val choosePngButton = JButton("更新资源…").apply {
            addActionListener {
                gui.showFileChooser { file ->
                    if (file == null) return@showFileChooser
                    if (!file.isFile) {
                        JOptionPane.showMessageDialog(
                            null,
                            "请选择 PNG 图片文件",
                            I18n.translate(I18nKeys.Dialog.TIP),
                            JOptionPane.WARNING_MESSAGE
                        )
                        return@showFileChooser
                    }
                    if (!file.name.endsWith(".png", ignoreCase = true)) {
                        JOptionPane.showMessageDialog(
                            null,
                            "当前仅支持 PNG（*.png）",
                            I18n.translate(I18nKeys.Dialog.TIP),
                            JOptionPane.WARNING_MESSAGE
                        )
                        return@showFileChooser
                    }
                    selectedPng = file
                    pngPathField.text = file.absolutePath
                    setPreviewFromFile(iconPreview, file)
                }
            }
        }

        val panel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(10, 12, 10, 12)
            preferredSize = Dimension(720, 360)
            add(
                JPanel(java.awt.GridBagLayout()).apply {
                    val c = java.awt.GridBagConstraints().apply {
                        insets = java.awt.Insets(6, 6, 6, 6)
                        fill = java.awt.GridBagConstraints.HORIZONTAL
                        weightx = 1.0
                        weighty = 0.0
                        gridx = 0
                        gridy = 0
                        anchor = java.awt.GridBagConstraints.WEST
                    }

                    fun row(label: String, comp: java.awt.Component, fillBoth: Boolean = false, weighty: Double = 0.0) {
                        c.gridx = 0
                        c.weightx = 0.0
                        c.weighty = 0.0
                        c.fill = java.awt.GridBagConstraints.NONE
                        add(JLabel(label), c)
                        c.gridx = 1
                        c.weightx = 1.0
                        c.weighty = weighty
                        c.fill = if (fillBoth) java.awt.GridBagConstraints.BOTH else java.awt.GridBagConstraints.HORIZONTAL
                        add(comp, c)
                        c.gridy++
                        c.weighty = 0.0
                    }

                    fun rowNoTitle(comp: java.awt.Component, fillBoth: Boolean = false, weighty: Double = 0.0) {
                        c.gridx = 0
                        c.gridwidth = 2
                        c.weightx = 1.0
                        c.weighty = weighty
                        c.fill = if (fillBoth) java.awt.GridBagConstraints.BOTH else java.awt.GridBagConstraints.HORIZONTAL
                        add(comp, c)
                        c.gridy++
                        c.gridwidth = 1
                        c.weighty = 0.0
                    }

                    val iconPreviewPanel = JPanel(java.awt.GridBagLayout()).apply {
                        add(
                            iconPreview,
                            java.awt.GridBagConstraints().apply {
                                anchor = java.awt.GridBagConstraints.CENTER
                            }
                        )
                    }
                    rowNoTitle(iconPreviewPanel)

                    row("图标资源", iconRefField)

                    row("图标尺寸", iconCandidatesScroll, fillBoth = true, weighty = 1.0)

                    val pngRow = JPanel(java.awt.GridBagLayout())
                    val pc = java.awt.GridBagConstraints().apply {
                        insets = java.awt.Insets(0, 0, 0, 6)
                        fill = java.awt.GridBagConstraints.HORIZONTAL
                        weightx = 1.0
                        gridx = 0
                        gridy = 0
                    }
                    pngRow.add(pngPathField, pc)
                    pc.gridx = 1
                    pc.weightx = 0.0
                    pc.insets = java.awt.Insets(0, 0, 0, 0)
                    pngRow.add(choosePngButton, pc)
                    row("更新资源", pngRow)

                    fun checkboxOnlyClickable(checkbox: JCheckBox): JPanel {
                        return JPanel(BorderLayout()).apply {
                            isOpaque = false
                            add(checkbox, BorderLayout.WEST)
                        }
                    }

                    c.gridx = 1
                    c.weightx = 1.0
                    c.fill = java.awt.GridBagConstraints.HORIZONTAL
                    add(checkboxOnlyClickable(generateMultiDensity), c)
                    c.gridy++

                    c.gridx = 1
                    c.weightx = 1.0
                    c.fill = java.awt.GridBagConstraints.HORIZONTAL
                    add(checkboxOnlyClickable(createMissing), c)
                    c.gridy++
                },
                BorderLayout.CENTER
            )
            add(
                JLabel("<html><small>保存后会按配置替换/生成 res/mipmap*/ 或 res/drawable*/ 下的同名图标文件（png/webp），并更新 AndroidManifest.xml。</small></html>"),
                BorderLayout.SOUTH
            )
        }

        val option = JOptionPane.showConfirmDialog(
            null,
            panel,
            I18n.translate(I18nKeys.Toolbar.EDIT_APP_ICONS),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        )
        if (option != JOptionPane.OK_OPTION) return

        val iconRef = iconRefField.text.trim()
        if (iconRef.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请填写图标资源引用，例如 @mipmap/ic_launcher", I18n.translate(I18nKeys.Dialog.TIP), JOptionPane.WARNING_MESSAGE)
            return
        }
        val png = selectedPng
        if (png == null) {
            JOptionPane.showMessageDialog(null, "请选择 PNG 图标文件", I18n.translate(I18nKeys.Dialog.TIP), JOptionPane.WARNING_MESSAGE)
            return
        }

        val update = AndroidAppInfoUpdate(
            packageName = null,
            labelText = null,
            useStringAppName = false,
            labelStringKey = "app_name",
            updateAllLocalesForAppName = false,
            appNameByValuesDir = null,
            removeStringFromValuesDirs = null,
            iconValue = iconRef,
            replaceIconPngFromFile = png,
            generateMultiDensityIcons = generateMultiDensity.isSelected,
            createMissingDensityIcons = createMissing.isSelected,
        )

        gui.showProgress("正在更新图标…", indeterminate = true)
        Thread {
            try {
                val result = AndroidAppInfoEditor.applyUpdate(workspaceRoot, update)
                SwingUtilities.invokeLater {
                    gui.hideProgress()
                    JOptionPane.showMessageDialog(
                        null,
                        result.message,
                        if (result.success) I18n.translate(I18nKeys.Dialog.INFO) else I18n.translate(I18nKeys.Dialog.ERROR),
                        if (result.success) JOptionPane.INFORMATION_MESSAGE else JOptionPane.ERROR_MESSAGE
                    )
                }
            } catch (e: Exception) {
                logger.error("更新图标失败", e)
                SwingUtilities.invokeLater {
                    gui.hideProgress()
                    JOptionPane.showMessageDialog(
                        null,
                        "更新失败：${e.message ?: "未知错误"}",
                        I18n.translate(I18nKeys.Dialog.ERROR),
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        }.start()
    }

    private fun setPreviewFromCandidates(preview: JLabel, candidates: List<IconCandidate>) {
        val sorted = candidates.sortedByDescending { (it.width ?: 0) * (it.height ?: 0) }
        // 优先尝试可读取的图片（通常为 PNG）
        for (c in sorted) {
            val before = preview.icon
            setPreviewFromFile(preview, c.file)
            if (preview.icon != null && preview.icon != before) return
        }
    }

    private fun setPreviewFromFile(preview: JLabel, file: File?) {
        if (file == null || !file.isFile) return
        val image = runCatching { ImageIO.read(file) }.getOrNull() ?: return
        val w = maxOf(
            if (preview.width > 0) preview.width else 0,
            preview.preferredSize.width
        )
        val h = maxOf(
            if (preview.height > 0) preview.height else 0,
            preview.preferredSize.height
        )
        val insets = preview.insets
        val aw = (w - insets.left - insets.right).coerceAtLeast(1)
        val ah = (h - insets.top - insets.bottom).coerceAtLeast(1)
        val sw = image.width.coerceAtLeast(1)
        val sh = image.height.coerceAtLeast(1)
        val scale = minOf(aw.toDouble() / sw, ah.toDouble() / sh)
        val dw = (sw * scale).toInt().coerceAtLeast(1)
        val dh = (sh * scale).toInt().coerceAtLeast(1)
        val scaled = image.getScaledInstance(dw, dh, Image.SCALE_SMOOTH)
        preview.icon = javax.swing.ImageIcon(scaled)
        preview.text = ""
    }
}
