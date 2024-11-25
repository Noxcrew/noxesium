plugins {
    id("fabric-loom")
}

repositories {
    maven { url = uri("https://maven.shedaniel.me/") }
}

dependencies {
    // Depend on the common project
    api(project(":common"))

    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Include dependencies in the jar
    include(project(":api"))
    include(project(":common"))
    include(libs.prtree)

    // Compatibility with other mods
    if (property("enableSodium") == "true") {
        modImplementation("maven.modrinth:sodium:${property("sodium")}")
    }
    if (property("enableModMenu") == "true") {
        modImplementation("com.terraformersmc:modmenu:${property("modmenu")}") {
            isTransitive = false
        }
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
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

        // Exclude sodium java classes when it's disabled
        if (project.property("enableSodium") != "true") {
            exclude("**/sodium/**.java")
        }
        if (project.property("enableModMenu") != "true") {
            exclude("**/modmenu/**.java")
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
