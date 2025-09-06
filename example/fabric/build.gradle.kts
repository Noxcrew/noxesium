plugins {
    id("fabric-loom")

    // This is a file defined in buildSrc which passes through
    // NMS sources properly.
    id("noxesium.example")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    // Rely on the Noxesium Fabric mod implementation as another mod, here because it's in the
    // same repository it's using this custom syntax, but you can use modImplementation!
    implementation(project(path = ":fabric", configuration = "namedElements"))
}