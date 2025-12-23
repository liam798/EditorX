package editorx.gui.settings

import editorx.core.i18n.I18n
import editorx.core.plugin.PluginManager
import editorx.gui.GuiEnvironment
import editorx.gui.main.MainWindow
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import java.util.Locale
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.UIManager
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.collections.asSequence

class SettingsDialog(
    owner: MainWindow,
    private val environment: GuiEnvironment,
    private val pluginManager: PluginManager,
    private val defaultSection: Section = Section.APPEARANCE,
) : JDialog(owner, if (I18n.locale().language == Locale.ENGLISH.language) "Settings" else "设置", true) {

    enum class Section { APPEARANCE, KEYMAP, PLUGINS, CACHE }

    private data class SectionItem(
        val section: Section,
        val titleZh: String,
        val titleEn: String,
    ) {
        fun title(locale: Locale): String =
            if (locale.language == Locale.ENGLISH.language) titleEn else titleZh
    }

    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)

    private val appearancePanel = AppearancePanel(environment.settings)
    private val keymapPanel = KeymapPanel()
    private val pluginsPanel = PluginsPanel(pluginManager)
    private val cachePanel = CachePanel(environment)

    private val titleLabel = JLabel()
    private val subtitleLabel = JLabel()
    private val navLabel = JLabel()

    private val listModel = DefaultListModel<SectionItem>().apply {
        addElement(SectionItem(Section.APPEARANCE, "外观", "Appearance"))
        addElement(SectionItem(Section.KEYMAP, "快捷键", "Keymap"))
        addElement(SectionItem(Section.PLUGINS, "插件", "Plugins"))
        addElement(SectionItem(Section.CACHE, "缓存", "Cache"))
    }

    private val navigation = JList(listModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean,
            ): java.awt.Component {
                val c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                val item = value as? SectionItem
                if (item != null) {
                    val locale = I18n.locale()
                    c.text = item.title(locale)
                    c.border = BorderFactory.createEmptyBorder(10, 14, 10, 12)
                    c.isOpaque = true
                    c.background = if (isSelected) UIManager.getColor("List.selectionBackground") else navigationBackground()
                    c.foreground = if (isSelected) UIManager.getColor("List.selectionForeground") else UIManager.getColor("Label.foreground")
                }
                return c
            }
        }
        selectionBackground = UIManager.getColor("List.selectionBackground")
        background = navigationBackground()
        border = BorderFactory.createEmptyBorder()
        addListSelectionListener {
            val item = selectedValue ?: return@addListSelectionListener
            showSection(item.section)
        }
    }

    private val i18nListener = {
        SwingUtilities.invokeLater {
            title = if (I18n.locale().language == Locale.ENGLISH.language) "Settings" else "设置"
            navigation.repaint()
            appearancePanel.refresh()
            updateHeaderTexts()
        }
    }

    init {
        background = UIManager.getColor("Panel.background")
        updateHeaderTexts()

        contentPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
        contentPanel.add(appearancePanel, Section.APPEARANCE.name)
        contentPanel.add(keymapPanel, Section.KEYMAP.name)
        contentPanel.add(pluginsPanel, Section.PLUGINS.name)
        contentPanel.add(cachePanel, Section.CACHE.name)

        layout = BorderLayout()
        add(buildHeaderPane(), BorderLayout.NORTH)
        add(buildBodyPane(), BorderLayout.CENTER)

        size = Dimension(960, 620)
        setLocationRelativeTo(owner)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        navigation.selectedIndex = listModel.elements().asSequence().indexOfFirst { it.section == defaultSection }
            .takeIf { it >= 0 } ?: 0

        I18n.addListener(i18nListener)

        addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosed(e: java.awt.event.WindowEvent?) {
                pluginsPanel.disposePanel()
                I18n.removeListener(i18nListener)
            }
        })
    }

    private fun showSection(section: Section) {
        cardLayout.show(contentPanel, section.name)
        when (section) {
            Section.APPEARANCE -> appearancePanel.refresh()
            Section.KEYMAP -> keymapPanel.refresh()
            Section.PLUGINS -> pluginsPanel.scanPlugins()
            Section.CACHE -> cachePanel.refresh()
        }
    }

    private fun buildHeaderPane(): JPanel {
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 20f)
        subtitleLabel.foreground = UIManager.getColor("Label.disabledForeground")
        subtitleLabel.border = BorderFactory.createEmptyBorder(4, 0, 0, 0)

        return JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(18, 24, 12, 24)
            isOpaque = false
            add(titleLabel, BorderLayout.NORTH)
            add(subtitleLabel, BorderLayout.SOUTH)
        }
    }

    private fun buildBodyPane(): JPanel {
        val navPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(12, 24, 18, 16)
            background = navigationBackground()
            navLabel.font = navLabel.font.deriveFont(Font.BOLD, 13f)
            navLabel.border = BorderFactory.createEmptyBorder(0, 0, 8, 0)
            navLabel.foreground = Color(0x55, 0x55, 0x55)
            add(navLabel, BorderLayout.NORTH)
            add(JScrollPane(navigation).apply {
                border = BorderFactory.createEmptyBorder()
                preferredSize = Dimension(220, 400)
                viewport.background = navigationBackground()
            }, BorderLayout.CENTER)
        }

        val contentWrapper = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(12, 0, 18, 24)
            isOpaque = false
            add(contentPanel, BorderLayout.CENTER)
        }

        return JPanel(BorderLayout()).apply {
            isOpaque = false
            add(navPanel, BorderLayout.WEST)
            add(contentWrapper, BorderLayout.CENTER)
        }
    }

    private fun updateHeaderTexts() {
        val english = I18n.locale().language == Locale.ENGLISH.language
        titleLabel.text = if (english) "Settings" else "设置"
        subtitleLabel.text = if (english) "Configure appearance, keymap, plugins and cache." else "配置外观、快捷键、插件与缓存等选项。"
        navLabel.text = if (english) "Sections" else "设置项"
    }

    private fun navigationBackground(): Color {
        val base = UIManager.getColor("Panel.background") ?: Color(0xF5F5F5)
        return if (base == Color.WHITE) Color(0xF5F7FA) else base.brighter()
    }
}
