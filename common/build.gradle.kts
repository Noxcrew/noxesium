plugins {
    id("net.neoforged.moddev")
    id("multiloader.common")
}

neoForge {
    neoFormVersion.set("${property("neo_form_version")}")

    // Validate AT files and raise errors when they have invalid targets
    // This option is false by default, but turning it on is recommended
    setAccessTransformers(file("src/main/resources/noxesium.cfg"))
    validateAccessTransformers = true
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