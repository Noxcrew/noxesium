plugins {
    id("fabric-loom") version "1.4-SNAPSHOT"
    id("noxesium.publishing")
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    maven {
        setUrl("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    mavenCentral()
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Add the API module
    include(project(":api"))
    api(project(":api"))

    // Compatibility with other mods
    modCompileOnly("maven.modrinth:sodium:${property("sodium")}")
    modCompileOnly("maven.modrinth:iris:${property("iris")}")
    if (property("enableSodium") == "true") {
        modRuntimeOnly("maven.modrinth:sodium:${property("sodium")}")
    }
    if (property("enableIris") == "true") {
        modRuntimeOnly("maven.modrinth:iris:${property("iris")}") {
            isTransitive = false
        }
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

loom {
    accessWidenerPath.set(file("src/main/resources/noxesium.accesswidener"))
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)

        // Exclude sodium java classes when it's disabled
        if (project.property("enableSodium") != "true") {
            exclude("**/sodium/**.java")
        }
    }

    withType<AbstractArchiveTask> {
        archiveBaseName.set("noxesium")
    }

    jar {
        from("LICENSE") {
            rename { return@rename "${it}_${rootProject.name}" }
        }
    }
}
