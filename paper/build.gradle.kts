import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.papermc.paperweight.userdev") version "1.5.15"
}

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    compileOnlyApi(libs.guava)

    api(libs.kotlin.coroutines)
    api(libs.slf4j)
    api(libs.caffeine)

    // Add the API module as a dependency
    api(project(":api"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = 21.toString()
            freeCompilerArgs += listOf("-Xexplicit-api=strict")
        }
    }
}