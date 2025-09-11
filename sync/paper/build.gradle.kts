import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("noxesium.sync")
    id("xyz.jpenilla.run-paper")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
    api(project(":paper:paper-platform"))

    // Add universal charset for detecting encodings
    implementation(libs.juniversalchardet)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    explicitApiMode.set(ExplicitApiMode.Strict)
}