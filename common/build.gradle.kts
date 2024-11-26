plugins {
    id("multiloader.common")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion.set("${property("neo_form_version")}")

    setAccessTransformers(file("src/main/resources/noxesium.cfg"))
    validateAccessTransformers = true

    interfaceInjectionData.from(file("src/main/resources/interface_injections.json"))
}

dependencies {
    // Include the API project
    api(project(":api"))

    // Include mixins with mixin extras
    implementation(libs.mixin)
    implementation(libs.mixinextras)
    annotationProcessor(libs.mixinextras)

    // Use PRTree as a custom dependency
    api(libs.prtree)
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
    add("commonResources", sourceSets["main"].resources.sourceDirectories.singleFile)
}