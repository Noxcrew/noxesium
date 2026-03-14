plugins {
    id("net.fabricmc.fabric-loom")

    // This is a file defined in buildSrc which passes through
    // NMS sources properly.
    id("noxesium.example")
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)
    implementation(project(":fabric"))
}