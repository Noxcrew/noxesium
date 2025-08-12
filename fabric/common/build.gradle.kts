plugins {
    id("fabric-loom")
    id("noxesium.common")
}

loom {
    accessWidenerPath.set(project(":fabric:fabric-api").file("src/main/resources/noxesium.accesswidener"))

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
        modImplementation(libs.sodium)
    }
    if (property("enableModMenu") == "true") {
        modImplementation(libs.modmenu) {
            isTransitive = false
        }
    }

    // Add DevLogin
    localRuntime("net.covers1624:DevLogin:0.1.0.5")

    // Pull in the Fabric API module code
    implementation(project(path = ":fabric:fabric-api", configuration = "namedElements"))

    // Include the base API and PRTree in the jar
    fun includeImplementation(target: Any) {
        include(target)
        implementation(target)
    }
    includeImplementation(project(":api"))
    includeImplementation(libs.prtree)
}

// Include the resources from the fabric-api folder too
tasks.processResources {
    from(project(":fabric:fabric-api").projectDir.resolve("src/main/resources"))
}