plugins {
    id("net.neoforged.moddev")
}

neoForge {
    // We currently only support NeoForge versions later than 21.0.x
    // See https://projects.neoforged.net/neoforged/neoforge for the latest updates
    version = "${property("forge_version")}"

    // Validate AT files and raise errors when they have invalid targets
    // This option is false by default, but turning it on is recommended
    setAccessTransformers(project(":common").file("src/main/resources/noxesium.cfg"))
    validateAccessTransformers = true

    runs {
        create("client") {
            client()
            ideName = "NeoForge Client (:${project.name})"
            gameDirectory.set(file("runs/client"))
        }
    }
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("noxesium.accesswidener")
    }
}