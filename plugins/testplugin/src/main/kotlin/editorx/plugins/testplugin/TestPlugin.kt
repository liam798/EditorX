package editorx.plugins.testplugin

import editorx.gui.CachedViewProvider
import editorx.gui.ViewProvider
import editorx.plugin.Plugin
import editorx.plugin.PluginContext
import editorx.plugin.PluginInfo
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * 测试插件
 */
class TestPlugin : Plugin {
    private var context: PluginContext? = null

    override fun getInfo(): PluginInfo {
        return PluginInfo(
            id = "test",
            name = "Test Plugin",
            version = "1.0",
        )
    }

    override fun activate(context: PluginContext) {
        this.context = context
        println("TestPlugin已启动")
        context.addActivityBarItem("", object : CachedViewProvider() {
            override fun createView(): JComponent {
                return JPanel()
            }
        })
    }

    override fun deactivate() {
        println("TestPlugin已停止")
    }
}
