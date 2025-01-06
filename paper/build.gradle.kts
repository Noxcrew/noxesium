import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    kotlin("jvm") version "2.1.0"
    alias(libs.plugins.paperweight)
    alias(libs.plugins.spotless)
}

val javaVersion: Int = 21

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    compileOnlyApi(libs.guava)

    api(libs.kotlin.coroutines)
    api(libs.kotlin.serialization.json)
    api(libs.kotlin.serialization.hocon)
    api(libs.slf4j)
    api(libs.caffeine)

    // Add the API module as a dependency
    api(project(":api"))
}

java {
    withJavadocJar()
    withSourcesJar()
}

configure<SpotlessExtension> {
    kotlin {
        ktlint("1.5.0")
        suppressLintsFor {
            step = "ktlint"
            shortCode = "standard:package-name"
        }
        suppressLintsFor {
            step = "ktlint"
            shortCode = "standard:annotation"
        }
        suppressLintsFor {
            step = "ktlint"
            shortCode = "standard:property-naming"
        }
    }
}

tasks.withType<KotlinCompile> {
    explicitApiMode.set(ExplicitApiMode.Strict)

    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
    }
}