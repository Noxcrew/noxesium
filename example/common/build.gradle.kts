plugins {
    id("net.fabricmc.fabric-loom")
}

dependencies {
    // Rely on the Minecraft sources themselves, but without Fabric's loader/API
    minecraft(libs.minecraft)

    // Replace this with a dependency on the Noxesium repository
    api(project(":api"))

    // This example shows how you can use a set-up that relies on NMS dependencies,
    // you can also make your own that does not use NMS dependencies by looking at how
    // this repository is set up. NMS-based implementations are likely most common though!
    api(project(":nms"))
}

// Define the sources of this repository as a new configuration which can be replied upon
// using `id("noxesium.example")` as a plugin (see buildSrc)
// This does mean this repository builds against Loom but if you don't use AW the source code
// will work fine on Paper.
configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
}