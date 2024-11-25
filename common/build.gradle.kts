plugins {
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion.set("${property("neo_form_version")}")
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