package editorx.plugin

import javax.swing.JComponent

/**
 * 视图提供器（UI 插件契约）
 */
interface ViewProvider {
    /** 返回要展示的视图组件 */
    fun getView(): JComponent
}

abstract class CachedViewProvider : ViewProvider {
    private var cachedInstance: JComponent? = null

    override fun getView(): JComponent {
        if (cachedInstance == null) {
            synchronized(this) {
                if (cachedInstance == null) {
                    cachedInstance = createView()
                }
            }
        }
        return cachedInstance!!
    }

    abstract fun createView(): JComponent
}
