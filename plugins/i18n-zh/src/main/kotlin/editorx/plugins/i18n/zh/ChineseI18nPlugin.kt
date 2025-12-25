package editorx.plugins.i18n.zh

import editorx.core.i18n.I18nKeys
import editorx.core.i18n.I18nPlugin
import editorx.core.plugin.PluginInfo
import java.util.Locale

private val dictionary = mapOf(
    I18nKeys.Menu.FILE to "文件",
    I18nKeys.Menu.EDIT to "编辑",
    I18nKeys.Menu.PLUGINS to "插件",
    I18nKeys.Menu.HELP to "帮助",
    I18nKeys.Menu.LANGUAGE to "语言",

    I18nKeys.Action.OPEN_FILE to "打开文件…",
    I18nKeys.Action.OPEN_FOLDER to "打开文件夹…",
    I18nKeys.Action.RECENT to "最近打开",
    I18nKeys.Action.SAVE to "保存",
    I18nKeys.Action.SAVE_AS to "另存为…",
    I18nKeys.Action.EXIT to "退出",
    I18nKeys.Action.UNDO to "撤销",
    I18nKeys.Action.REDO to "重做",
    I18nKeys.Action.FIND to "查找…",
    I18nKeys.Action.REPLACE to "替换…",
    I18nKeys.Action.GLOBAL_SEARCH to "在文件中搜索…",
    I18nKeys.Action.PLUGIN_MANAGER to "插件管理",
    I18nKeys.Action.ABOUT to "关于",
    I18nKeys.Action.HELP to "帮助文档",
    I18nKeys.Action.NEW_FILE to "新建文件",
    I18nKeys.Action.NEW_FOLDER to "新建文件夹",
    I18nKeys.Action.DELETE to "删除",
    I18nKeys.Action.REFRESH to "刷新",
    I18nKeys.Action.REVEAL_IN_SYSTEM to "在系统中显示",
    I18nKeys.Action.COPY_PATH to "复制路径",
    I18nKeys.Action.SELECT_IN_SIDEBAR to "在侧栏中选中",
    I18nKeys.Action.REVEAL_IN_EXPLORER to "在资源管理器中显示",
    I18nKeys.Action.CLOSE to "关闭",
    I18nKeys.Action.CLOSE_OTHERS to "关闭其他标签",
    I18nKeys.Action.CLOSE_ALL to "关闭所有标签",
    I18nKeys.Action.CLOSE_LEFT to "关闭左侧标签",
    I18nKeys.Action.CLOSE_RIGHT to "关闭右侧标签",
    I18nKeys.Action.CLOSE_UNMODIFIED to "关闭未修改标签",
    I18nKeys.Action.FORMAT_FILE to "格式化文件",
    I18nKeys.Action.RESET to "重置",
    I18nKeys.Action.CANCEL to "取消",
    I18nKeys.Action.CONFIRM to "确定",
    I18nKeys.Action.INSTALL_PLUGIN to "安装插件",
    I18nKeys.Action.OPEN_PLUGINS_FOLDER to "打开插件目录",
    I18nKeys.Action.ENABLE to "启用",
    I18nKeys.Action.DISABLE to "禁用",
    I18nKeys.Action.UNINSTALL to "卸载",

    I18nKeys.Settings.TITLE to "设置",
    I18nKeys.Settings.PREFERENCES to "设置项",
    I18nKeys.Settings.APPEARANCE to "外观",
    I18nKeys.Settings.LANGUAGE to "语言",
    I18nKeys.Settings.THEME to "主题",
    I18nKeys.Settings.APPEARANCE_TIP to "提示：语言和主题切换立即生效。",
    I18nKeys.Settings.KEYMAP to "快捷键",
    I18nKeys.Settings.PLUGINS to "插件",
    I18nKeys.Settings.CACHE to "缓存",
    I18nKeys.Settings.KEYMAP_TITLE to "快捷键",
    I18nKeys.Settings.KEYMAP_HINT to "<html>当前列表展示默认快捷键，自定义与导出功能规划中。</html>",
    I18nKeys.Settings.ADD_NOTE to "添加备注…",
    I18nKeys.Settings.ADD_NOTE_TOOLTIP to "快捷键自定义功能开发中",
    I18nKeys.Settings.EXPORT to "导出配置…",
    I18nKeys.Settings.EXPORT_TOOLTIP to "功能开发中",
    I18nKeys.Settings.CACHE_TITLE to "缓存",
    I18nKeys.Settings.CACHE_HINT to "清理缓存前请确认不存在正在运行的反编译或插件任务。",
    I18nKeys.Settings.REFRESH_CACHE to "刷新",
    I18nKeys.Settings.CLEAR_SELECTED to "清理所选",
    I18nKeys.Settings.OPEN_FOLDER to "打开所在目录",
    I18nKeys.Settings.PLUGIN_STATE_DISABLED to "已禁用",
    I18nKeys.Settings.PLUGIN_STATE_ENABLED to "已启用",
    I18nKeys.Settings.PLUGIN_STATE_FAILED to "失败",

    I18nKeys.CacheTable.NAME to "名称",
    I18nKeys.CacheTable.PATH to "路径",
    I18nKeys.CacheTable.SIZE to "大小",
    I18nKeys.CacheTable.DESCRIPTION to "说明",

    I18nKeys.Theme.LIGHT to "浅色",
    I18nKeys.Theme.DARK to "深色",

    I18nKeys.Status.READY to "就绪",
    I18nKeys.Status.CANCEL to "取消",
    I18nKeys.Status.GOTO_LINE_COLUMN to "转到行/列",
    I18nKeys.Status.ERROR to "错误",
    I18nKeys.Status.WARNING to "警告",
    I18nKeys.Status.SUCCESS to "成功",
    I18nKeys.Status.VERSION_CONTROL to "版本控制",
    I18nKeys.Status.NO_FILE_OPENED to "未打开文件",
    I18nKeys.Status.LINE_COLUMN to "行 %d, 列 %d",

    I18nKeys.FindReplace.SEARCH to "搜索",
    I18nKeys.FindReplace.REPLACE to "替换",
    I18nKeys.FindReplace.EXPAND_REPLACE to "展开替换",
    I18nKeys.FindReplace.COLLAPSE_REPLACE to "收起替换",
    I18nKeys.FindReplace.MATCH_CASE to "区分大小写",
    I18nKeys.FindReplace.WHOLE_WORD to "全词匹配",
    I18nKeys.FindReplace.REGEX to "正则表达式",
    I18nKeys.FindReplace.REPLACE_ONE to "替换",
    I18nKeys.FindReplace.REPLACE_ALL to "全部替换",
    I18nKeys.FindReplace.REPLACE_ONE_TOOLTIP to "替换当前匹配",
    I18nKeys.FindReplace.REPLACE_ALL_TOOLTIP to "替换所有匹配",
    I18nKeys.FindReplace.FIND_PREV to "上一个（Shift+Enter）",
    I18nKeys.FindReplace.FIND_NEXT to "下一个（Enter）",
    I18nKeys.FindReplace.CLOSE to "关闭",
    I18nKeys.FindReplace.NO_RESULTS to "0 个结果",
    I18nKeys.FindReplace.RESULTS to "%d/%d",
    I18nKeys.FindReplace.INVALID_REGEX to "正则表达式不合法",
    I18nKeys.FindReplace.SEARCH_FAILED to "搜索失败",
    I18nKeys.FindReplace.REPLACE_FAILED to "替换失败",

    I18nKeys.Dialog.SELECT_FOLDER to "选择文件夹",
    I18nKeys.Dialog.TIP to "提示",
    I18nKeys.Dialog.ABOUT_TITLE to "关于 EditorX",
    I18nKeys.Dialog.ABOUT_MESSAGE to """EditorX v1.0

一个用于编辑APK文件的工具

功能特性：
• 语法高亮编辑
• 插件系统支持
• 多标签页界面
• 文件浏览和管理

开发：XiaMao Tools""",
    I18nKeys.Dialog.HELP_NOT_IMPLEMENTED to "帮助文档待实现",
    I18nKeys.Dialog.NO_RECENT_FILES to "(无)",
    I18nKeys.Dialog.FILE_NOT_EXISTS to "文件不存在",
    I18nKeys.Dialog.NOT_FOUND to "未找到",
    I18nKeys.Dialog.ERROR to "错误",
    I18nKeys.Dialog.INFO to "提示",
    I18nKeys.Dialog.SELECT_ENTRY_FIRST to "请选择要清理的缓存条目",
    I18nKeys.Dialog.DIRECTORY_NOT_FOUND to "目录不存在：%s",
    I18nKeys.Dialog.CLEAR_CACHE to "清理缓存",
    I18nKeys.Dialog.CLEARED to "已清理",
    I18nKeys.Dialog.CLEAR_FAILED to "清理失败，请检查文件是否被占用",
    I18nKeys.Dialog.UNABLE_TO_OPEN to "无法打开目录：%s",

    I18nKeys.Explorer.TITLE to "资源管理器",
    I18nKeys.Explorer.NEW_FILE to "新建文件",
    I18nKeys.Explorer.NEW_FOLDER to "新建文件夹",
    I18nKeys.Explorer.DELETE to "删除",
    I18nKeys.Explorer.REFRESH to "刷新",
    I18nKeys.Explorer.REVEAL_IN_SYSTEM to "在系统中显示",

    I18nKeys.Editor.CLOSE to "关闭",
    I18nKeys.Editor.CLOSE_OTHERS to "关闭其他标签",
    I18nKeys.Editor.CLOSE_ALL to "关闭所有标签",
    I18nKeys.Editor.CLOSE_LEFT to "关闭左侧标签",
    I18nKeys.Editor.CLOSE_RIGHT to "关闭右侧标签",
    I18nKeys.Editor.CLOSE_UNMODIFIED to "关闭未修改标签",
    I18nKeys.Editor.FORMAT_FILE to "格式化文件",
    I18nKeys.Editor.CANNOT_READ_FILE to "无法读取文件: %s",
    I18nKeys.Editor.TOTAL_FILES to "共 %d 个文件/目录",
    I18nKeys.Editor.CANNOT_READ_ARCHIVE to "无法读取压缩包",

    I18nKeys.Toolbar.GOTO_MANIFEST to "跳转到 AndroidManifest.xml",
    I18nKeys.Toolbar.GOTO_MAIN_ACTIVITY to "跳转到 MainActivity",
    I18nKeys.Toolbar.GOTO_APPLICATION to "跳转到 Application",
    I18nKeys.Toolbar.BUILD to "构建",
    I18nKeys.Toolbar.TOGGLE_SIDEBAR to "切换侧边栏",
    I18nKeys.Toolbar.GLOBAL_SEARCH to "全局搜索",
    I18nKeys.Toolbar.SETTINGS to "设置",
    I18nKeys.Toolbar.DOUBLE_SHIFT to "双击Shift",

    I18nKeys.Search.SEARCH to "搜索",
    I18nKeys.Search.STOP to "停止",
    I18nKeys.Search.SEARCH_LABEL to "搜索：",
    I18nKeys.Search.PLEASE_ENTER_SEARCH to "请输入要搜索的内容",
    I18nKeys.Search.PLEASE_OPEN_FOLDER to "请先打开文件夹（工作区），再进行全局搜索",
    I18nKeys.Search.SCANNED_FILES to "已扫描 %d 个文件，找到 %d 条结果",

    I18nKeys.Welcome.RECENT_PROJECTS to "Recent projects",
    I18nKeys.Welcome.VIEW_ALL to "View all (%d)",
    I18nKeys.Welcome.NO_RECENT_PROJECTS to "No recent projects",

    // 语言名称使用动态生成的 key
    I18nKeys.Lang.forLocale(Locale.forLanguageTag("zh")) to "中文",
    I18nKeys.Lang.forLocale(Locale.SIMPLIFIED_CHINESE) to "中文（简体）",
    I18nKeys.Lang.forLocale(Locale.TRADITIONAL_CHINESE) to "中文（繁体）",
    I18nKeys.Lang.forLocale(Locale.ENGLISH) to "English",
)

class ChineseI18nPlugin : I18nPlugin(Locale.SIMPLIFIED_CHINESE) {

    override fun getInfo() = PluginInfo(
        id = "i18n-zh",
        name = "Chinese (i18n)",
        version = "0.0.1",
    )

    override fun translate(key: String): String? = dictionary[key]
}

