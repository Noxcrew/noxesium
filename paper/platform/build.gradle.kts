import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
    id("noxesium.nms")
}

val javaVersion: Int = 21

dependencies {
    paperweight.paperDevBundle("${property("paper_version")}")
    compileOnlyApi(libs.guava)

    api(libs.kotlin.coroutines)
    api(libs.kotlin.serialization.json)
    api(libs.slf4j)
    api(libs.caffeine)
    api(libs.viaversion)
    api(libs.prtree)

    // Add the API module as a dependency
    api(project(":api"))
    api(project(":paper:paper-api"))
    api(project(":paper:paper-packet"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

// Configure any existing RunServerTasks
tasks.withType<RunServer> {
    jvmArgs("-Dio.papermc.paper.suppress.sout.nags=true")
}

tasks.withType<KotlinCompile> {
    explicitApiMode.set(ExplicitApiMode.Strict)

    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
    }
}