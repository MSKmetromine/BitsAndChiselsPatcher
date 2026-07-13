import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties

val mcVersion = providers.gradleProperty("minecraft_version").orNull ?: "1.18.2"

val propertiesPath = Paths.get("versions", "${mcVersion}.properties")

val properties = Properties()
properties.load(Files.newInputStream(propertiesPath))

for ((key, value) in properties) {
    gradle.extra.set(key.toString(), value)
}

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}
