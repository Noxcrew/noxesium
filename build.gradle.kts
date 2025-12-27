import org.gradle.jvm.tasks.Jar
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import noxesium.GitCommitIdValueSource

plugins {
    id("noxesium.publishing") apply false

    kotlin("jvm") version "2.2.0" apply false
    alias(libs.plugins.moddev) apply false
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.paperweight) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.run.paper) apply false
}

val javaVersion: Int = 21

allprojects {
    group = "com.noxcrew.noxesium"

    val gitCommitId = providers.of(GitCommitIdValueSource::class.java) {
        parameters.rootDirectory.set(rootProject.projectDir)
    }.get()
    version = "${property("mod_version")}+${gitCommitId}"

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.covers1624.net/")
        maven("https://repo.viaversion.com")
        mavenCentral()
        maven {
            setUrl("https://api.modrinth.com/maven")
            content {
                includeGroup("maven.modrinth")
            }
        }
    }
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<SpotlessPlugin>()

    tasks {
        withType<Jar> {
            from("LICENSE") {
                rename { return@rename "${it}_${rootProject.name}" }
            }
        }

        withType<JavaCompile> {
            options.release.set(javaVersion)
            options.encoding = Charsets.UTF_8.name()
            sourceCompatibility = javaVersion.toString()
            targetCompatibility = javaVersion.toString()
        }

        withType<AbstractArchiveTask> {
            archiveBaseName.set("noxesium-${project.name}")
        }
    }

    extensions.configure<SpotlessExtension> {
        java {
            palantirJavaFormat("2.50.0")
        }
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

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }

    // Set this late as the Kotlin plugin may not be applied yet!
    afterEvaluate {
        tasks {
            withType<KotlinCompile> {
                explicitApiMode.set(ExplicitApiMode.Strict)

                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
                }
            }
        }
    }
}
