import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("noxesium.publishing")
}

dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
    compileOnlyApi(libs.guava)
    api(libs.slf4j)

    // Use the NMS project to get NMS classes for compilation!
    compileOnly(project(":nms"))
}