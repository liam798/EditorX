package editorx.gui.settings

import editorx.core.i18n.I18n
import java.awt.BorderLayout
import java.awt.Font
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class AppearancePanel : JPanel(BorderLayout()) {
    private val zhButton = JRadioButton()
    private val enButton = JRadioButton()
    private val headerLabel = JLabel()
    private val footerLabel = JLabel()
    private val languagePanel = JPanel()

    init {
        border = BorderFactory.createEmptyBorder(16, 16, 16, 16)

        headerLabel.font = headerLabel.font.deriveFont(Font.BOLD, 16f)
        headerLabel.border = BorderFactory.createEmptyBorder(0, 0, 12, 0)

        ButtonGroup().apply {
            add(zhButton)
            add(enButton)
        }

        languagePanel.layout = BoxLayout(languagePanel, BoxLayout.Y_AXIS)
        zhButton.alignmentX = LEFT_ALIGNMENT
        enButton.alignmentX = LEFT_ALIGNMENT
        zhButton.addActionListener { I18n.setLocale(Locale.SIMPLIFIED_CHINESE) }
        enButton.addActionListener { I18n.setLocale(Locale.ENGLISH) }
        languagePanel.add(zhButton)
        languagePanel.add(Box.createVerticalStrut(8))
        languagePanel.add(enButton)

        footerLabel.border = BorderFactory.createEmptyBorder(12, 0, 0, 0)

        add(headerLabel, BorderLayout.NORTH)
        add(languagePanel, BorderLayout.CENTER)
        add(footerLabel, BorderLayout.SOUTH)

        refresh()
    }

    fun refresh() {
        val locale = I18n.locale()
        if (locale.language == Locale.ENGLISH.language) {
            enButton.isSelected = true
            zhButton.text = "Chinese (Simplified)"
            enButton.text = "English"
            headerLabel.text = "Appearance"
            languagePanel.border = BorderFactory.createTitledBorder("Language")
            footerLabel.text = "Tip: language changes apply immediately; some plugin texts require matching language packs."
        } else {
            zhButton.isSelected = true
            zhButton.text = "中文（简体）"
            enButton.text = "English"
            headerLabel.text = "外观"
            languagePanel.border = BorderFactory.createTitledBorder("语言")
            footerLabel.text = "提示：语言切换立即生效，部分插件文案需对应语言包支持。"
        }
    }
}
