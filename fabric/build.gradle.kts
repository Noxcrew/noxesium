plugins {
    id("fabric-loom")
    id("multiloader.loader")
}

repositories {
    maven { url = uri("https://maven.shedaniel.me/") }
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/noxesium.accesswidener"))

    mixin {
        defaultRefmapName.set("noxesium.refmap.json")
    }
}

dependencies {
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

    // Include dependencies in jar
    project(":api").apply {
        include(this)
        api(this)
    }
    include("org.khelekore:prtree:1.5")
    implementation("org.khelekore:prtree:1.5")
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("noxesium.cfg")
    }
}