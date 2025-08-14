import java.io.ByteArrayOutputStream
import org.gradle.jvm.tasks.Jar
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin

fun getGitCommit(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

plugins {
    id("noxesium.publishing") apply false

    alias(libs.plugins.loom) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.paperweight) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.run.paper) apply false
}

val javaVersion: Int = 21

allprojects {
    group = "com.noxcrew.noxesium"
    version = "${property("mod_version")}+${getGitCommit()}"

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
    apply<Noxesium_publishingPlugin>()
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
}
