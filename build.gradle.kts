import java.io.ByteArrayOutputStream

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
    alias(libs.plugins.moddev) apply false
}

val javaVersion: Int = 21

allprojects {
    group = "com.noxcrew.noxesium"
    version = "${property("mod_version")}+${getGitCommit()}"

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.terraformersmc.com/")
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

    tasks.withType<JavaCompile> {
        options.release.set(javaVersion)
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
