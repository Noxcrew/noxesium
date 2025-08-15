import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    kotlin("jvm") version "2.2.0"
    id("io.papermc.paperweight.userdev")
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
}

val javaVersion: Int = 21

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    compileOnlyApi(libs.guava)

    api(libs.kotlin.coroutines)
    api(libs.kotlin.serialization.json)
    api(libs.kotlin.serialization.hocon)
    api(libs.slf4j)
    api(libs.caffeine)
    api(libs.viaversion)

    // Add the API module as a dependency
    api(project(":api"))

    // Include the nms project as a dependency
    implementation(project(path = ":nms", configuration = "namedElements"))
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