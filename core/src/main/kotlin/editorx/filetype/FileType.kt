package editorx.filetype

import javax.swing.Icon

interface FileType {

    fun getName(): String

    fun getDisplayName(): String {
        return getName()
    }

    fun getExtensions(): Array<String>

    fun getIcon(): Icon?
}

