plugins {
    id("net.neoforged.moddev")
    id("multiloader.loader")
}

neoForge {
    // We currently only support NeoForge versions later than 21.0.x
    // See https://projects.neoforged.net/neoforged/neoforge for the latest updates
    version = "${property("neoforge_version")}"

    setAccessTransformers(project(":common").file("src/main/resources/noxesium.cfg"))
    validateAccessTransformers = true

    interfaceInjectionData.from(project(":common").file("src/main/resources/interface_injections.json"))

    runs {
        create("client") {
            client()
            ideName = "Run NeoForge Client"
            gameDirectory.set(file("runs/client"))
        }
    }

    mods {
        register("noxesium") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    // Define a function for adding included implementations
    fun includeImplementation(target: Any) {
        jarJar(target)
        additionalRuntimeClasspath(target)
    }

    // Include dependencies in jar and at runtime
    includeImplementation(project(":api"))
    includeImplementation(libs.prtree)
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("noxesium.accesswidener")
    }
}