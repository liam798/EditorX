package editorx.core.plugin.gui

import java.io.File

/**
 * 文件处理器注册表
 * 用于管理所有注册的文件处理器
 */
object FileHandlerRegistry {
    private val fileHandlers = mutableListOf<FileHandler>()

    /**
     * 注册文件处理器
     * @param handler 文件处理器
     */
    fun register(handler: FileHandler) {
        fileHandlers.add(handler)
    }

    /**
     * 取消注册文件处理器
     * @param handler 文件处理器
     */
    fun unregister(handler: FileHandler) {
        fileHandlers.remove(handler)
    }

    /**
     * 处理文件打开事件
     * 先询问所有注册的文件处理器，如果某个处理器返回 true，则不再继续处理
     * @param file 要打开的文件
     * @return 如果已被处理器处理返回 true，否则返回 false
     */
    fun handleOpenFile(file: File): Boolean {
        for (handler in fileHandlers) {
            if (handler.canHandle(file)) {
                if (handler.handleOpenFile(file)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 获取所有注册的文件处理器
     */
    fun getAllHandlers(): List<FileHandler> {
        return fileHandlers.toList()
    }

    /**
     * 清空所有注册的文件处理器
     */
    fun clear() {
        fileHandlers.clear()
    }
}

