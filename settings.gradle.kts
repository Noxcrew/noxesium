rootProject.name = "noxesium"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        gradlePluginPortal()
    }
}

include("api")
include("common")
include("fabric")
// include("neoforge")
include("paper")