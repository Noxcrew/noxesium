import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.papermc.paperweight.userdev")
    id("noxesium.publishing")
}

dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
    compileOnlyApi(libs.guava)

    api(libs.kotlin.coroutines)
    api(libs.kotlin.serialization.json)
    api(libs.slf4j)
    api(libs.caffeine)

    // Add the API module as a dependency
    api(project(":api"))
}