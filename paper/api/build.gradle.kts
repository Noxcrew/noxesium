plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("noxesium.publishing")
}


dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
}