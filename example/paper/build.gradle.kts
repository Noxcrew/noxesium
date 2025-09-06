plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")

    // This is a file defined in buildSrc which passes through
    // NMS sources properly.
    id("noxesium.example")
}

dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
    compileOnlyApi(libs.guava)

    // Replace these with a dependency on the Noxesium repository's paper platform
    api(project(":paper:paper-platform"))
}

java {
    withJavadocJar()
    withSourcesJar()
}