plugins {
    id("fabric-loom")
    id("multiloader.loader")
}

repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://maven.covers1624.net/")
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/noxesium.accesswidener"))

    mixin {
        defaultRefmapName.set("noxesium.refmap.json")
    }

    runs {
        create("clientAuth") {
            client()
            ideConfigGenerated(true)
            programArgs.addAll(listOf("--launch_target", "net.fabricmc.loader.impl.launch.knot.KnotClient"))
            mainClass.set("net.covers1624.devlogin.DevLogin")
        }
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

    // Add DevLogin
    localRuntime("net.covers1624:DevLogin:0.1.0.5")

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