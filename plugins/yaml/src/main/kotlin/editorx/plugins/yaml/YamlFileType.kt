import editorx.filetype.LanguageFileType
import editorx.gui.IconRef
import editorx.plugins.yaml.YamlIcons

object YamlFileType : LanguageFileType(YamlLanguage) {
    const val DEFAULT_EXTENSION: String = "yml"

    override fun getName(): String = "yaml"

    override fun getDescription(): String = "YAML files"

    override fun getDefaultExtension(): String = DEFAULT_EXTENSION

    override fun getIcon(): IconRef = YamlIcons.YamlFile
}
