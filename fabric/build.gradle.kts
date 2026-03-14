plugins {
    id("net.fabricmc.fabric-loom")
    id("noxesium.fabric")
    id("noxesium.nms")
    id("noxesium.publishing")
}

loom {
    accessWidenerPath.set(file("src/main/resources/noxesium.accesswidener"))

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
    api(libs.fabric.loader)
    api(libs.fabric.api)

    // Compatibility with other mods
    if (property("enableSodium") == "true") {
        implementation(libs.sodium)
    }
    if (property("enableModMenu") == "true") {
        implementation(libs.modmenu) {
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
    includeLibrary(project(":api"))
    includeLibrary(libs.prtree)
    includeLibrary(libs.adventure.fabric)
}
