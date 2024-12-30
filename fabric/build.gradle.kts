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
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    // Compatibility with other mods
    if (property("enableSodium") == "true") {
        modImplementation(libs.sodium.fabric)
    }
    if (property("enableModMenu") == "true") {
        modImplementation(libs.modmenu) {
            isTransitive = false
        }
    }

    // Define a function for adding included implementations
    fun includeImplementation(target: Any) {
        include(target)
        implementation(target)
    }

    // Include dependencies in jar but don't mark them as api
    includeImplementation(project(":api"))
    includeImplementation(libs.prtree)
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("noxesium.cfg")
    }
}