plugins {
    id("net.neoforged.moddev")
}

neoForge {
    setNeoFormVersion("${property("neoform_version")}")
}

dependencies {
    // Replace this with a dependency on the Noxesium repository
    api(project(":api"))

    // This example shows how you can use a set-up that relies on NMS dependencies,
    // you can also make your own that does not use NMS dependencies by looking at how
    // this repository is set up. NMS-based implementations are likely most common though!
    api(project(":nms"))
}

// Define the sources of this repository as a new configuration which can be replied upon
// using `id("noxesium.example")` as a plugin (see buildSrc)
// This does mean this repository builds against neoform but it's the only available platform
// that does not cause any issues. Paperweight and Loom both do not work as a shared platform.
configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
}