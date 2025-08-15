plugins {
    id("fabric-loom")
    id("noxesium.fabric")
}

loom {
    accessWidenerPath.set(file("src/main/resources/noxesium.accesswidener"))

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
    modApi(libs.fabric.loader)
    modApi(libs.fabric.api)

    // Include NMS sources as a mod (remapping it)
    api(project(path = ":nms", configuration = "namedElements"))

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
    localRuntime(libs.devlogin)

    // Include the base API, PRTree, and Adventure in the jar
    fun includeLibrary(target: Any) {
        include(target)
        api(target)
    }
    fun includeMod(target: Any) {
        include(target)
        modApi(target)
    }
    includeLibrary(project(":api"))
    includeLibrary(libs.prtree)
    includeMod(libs.adventure.fabric)
}
