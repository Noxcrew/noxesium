rootProject.name = "noxesium"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

include("api")
include("fabric")