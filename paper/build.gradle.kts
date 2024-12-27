import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

dependencies {
    paperweight.paperDevBundle("1.21-R0.1-SNAPSHOT")
    compileOnlyApi(libs.guava)

    api(libs.kotlin.coroutines)
    api(libs.kotlin.serialization.json)
    api(libs.kotlin.serialization.hocon)
    api(libs.slf4j)
    api(libs.caffeine)
    api(libs.bundles.cloud)

    // Add the API module as a dependency
    api(project(":api"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = 21.toString()
            freeCompilerArgs += listOf("-Xexplicit-api=strict")
        }
    }
}