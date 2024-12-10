plugins {
    id("net.neoforged.moddev")
    id("multiloader.loader")
}

neoForge {
    // We currently only support NeoForge versions later than 21.0.x
    // See https://projects.neoforged.net/neoforged/neoforge for the latest updates
    version = "${property("forge_version")}"

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
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    // Include dependencies in jar
    implementation(project(":api"))
    implementation(libs.prtree)
}

tasks {
    named<ProcessResources>("processResources").configure {
        exclude("noxesium.accesswidener")
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}