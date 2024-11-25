plugins {
    id("fabric-loom")
    id("multiloader.loader")
}

repositories {
    maven { url = uri("https://maven.shedaniel.me/") }
}

dependencies {
    // Depend on api and prtree
    api(project(":api"))
    api(libs.prtree)

    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    // Compatibility with other mods
    if (property("enableSodium") == "true") {
        modImplementation("maven.modrinth:sodium:${property("sodium")}")
    }
    if (property("enableModMenu") == "true") {
        modImplementation("com.terraformersmc:modmenu:${property("modmenu")}") {
            isTransitive = false
        }
    }

    // Include dependencies in the jar
    include(project(":api"))
    include(project(":common"))
    include(libs.prtree)
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/noxesium.accesswidener"))
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("noxesium.cfg")
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
}
