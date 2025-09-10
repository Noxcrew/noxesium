plugins {
    kotlin("jvm")
    id("noxesium.sync")
    id("xyz.jpenilla.run-paper")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
    api(project(":paper:paper-platform"))
}

java {
    withJavadocJar()
    withSourcesJar()
}