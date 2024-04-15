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
    id("java-library")
    id("noxesium.publishing")
}

val javaVersion: Int = 21

allprojects {
    group = "com.noxcrew.noxesium"
    version = "${property("mod_version")}+${getGitCommit()}"
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<Noxesium_publishingPlugin>()

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
    }

    tasks.withType<JavaCompile> {
        options.release.set(javaVersion)
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
