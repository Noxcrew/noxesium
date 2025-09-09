plugins {
    id("fabric-loom")
    id("noxesium.fabric")
    id("noxesium.sync")
}

loom {
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
    implementation(project(path = ":fabric", configuration = "namedElements"))

    modImplementation(libs.modmenu) {
        isTransitive = false
    }

    // Add DevLogin
    localRuntime(libs.devlogin)

    // Add tiny filled dialogs for the file picker
    val lwjglVersion = "3.3.2"
    implementation("org.lwjgl:lwjgl-tinyfd:${lwjglVersion}")
    runtimeOnly("org.lwjgl:lwjgl-tinyfd:${lwjglVersion}:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-tinyfd:${lwjglVersion}:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-tinyfd:${lwjglVersion}:natives-macos")
}