import editorx.gui.IconRef
import editorx.lang.LanguageFileType

class YamlFileType : LanguageFileType(YamlLanguage.getInstance()) {
    override fun getName(): String {
        return "yaml"
    }

    override fun getDescription(): String {
        return "YAML files"
    }

    override fun getExtensions(): Set<String> {
        return setOf("yaml", "yml")
    }

    override fun getIcon(): IconRef {
        return IconRef("icons/yaml.svg", YamlFileType::class.java.classLoader)
    }
}
