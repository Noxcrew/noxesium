plugins {
    id("net.fabricmc.fabric-loom")
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
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(project(":fabric"))

    implementation(libs.modmenu) {
        isTransitive = false
    }

    // Add DevLogin
    localRuntime(libs.devlogin)

    // Add universal charset for detecting encodings
    fun includeLibrary(target: Any) {
        include(target)
        api(target)
    }
    includeLibrary(libs.juniversalchardet)
}