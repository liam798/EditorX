plugins {
    id("buildsrc.convention.kotlin-jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}

afterEvaluate {
    tasks.jar {
        archiveBaseName.set("explorer")
        archiveVersion.set("1.0.0")

        manifest {
            attributes(
                "Plugin-Id" to "explorer",
                "Plugin-Name" to "Explorer",
                "Plugin-Version" to archiveVersion,
                "Main-Class" to "editorx.plugins.explorer.ExplorerPlugin"
            )
        }
    }
}
