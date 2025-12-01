package editorx.gui.dialog

import editorx.core.plugin.PluginManager
import java.awt.BorderLayout
import javax.swing.*

class PluginManagerDialog(owner: JFrame, private val pluginManager: PluginManager) : JDialog(owner, "插件管理", true) {
    private val model = DefaultListModel<String>()
    private val list = JList(model)

    init {
        layout = BorderLayout()
        add(JScrollPane(list), BorderLayout.CENTER)
        val refreshBtn = JButton("刷新").apply { addActionListener { refresh() } }
        val closeBtn = JButton("关闭").apply { addActionListener { dispose() } }
        val panel = JPanel().apply { add(refreshBtn); add(closeBtn) }
        add(panel, BorderLayout.SOUTH)
        setSize(420, 360)
        setLocationRelativeTo(owner)
        refresh()
    }

    private fun refresh() {
        model.clear()
        pluginManager.getAllPluginContexts()
            .forEach { p -> model.addElement("${p.pluginInfo().name} (${p.pluginInfo().version}) - ${p.pluginInfo().id}") }
    }
}

