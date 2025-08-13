rootProject.name = "noxesium"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

fun includeGroup(
    group: String,
    vararg modules: String,
) = modules.forEach {
    val first = "$group:$it"
    val second = first.replace(":", "-")
    include(first)
    project(":$first").name = second
}

include("api")
include("nms")
include("paper")

includeGroup(
    "fabric",
    "platform",
    "example"
)