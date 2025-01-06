plugins {
    id("multiloader.common")
    id("net.neoforged.moddev")
}

neoForge {
    setNeoFormVersion("${property("neoform_version")}")

    setAccessTransformers(file("src/main/resources/noxesium.cfg"))
    validateAccessTransformers = true

    interfaceInjectionData.from(file("src/main/resources/interface_injections.json"))
}

dependencies {
    // Include the API project
    compileOnlyApi(project(":api"))

    // Include mixins with mixin extras
    compileOnly(libs.mixin)
    compileOnly(libs.mixinextras)
    annotationProcessor(libs.mixinextras)

    // Use PRTree as a custom dependency but not as API
    compileOnly(libs.prtree)

    // Add Sodium as a compile dependency available to all subprojects
    if (property("enableSodium") == "true") {
        compileOnly(libs.sodium.neoforge)
    }
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