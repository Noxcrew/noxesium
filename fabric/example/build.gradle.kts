plugins {
    id("fabric-loom")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    // modImplementation(project(path = ":fabric:fabric-common", configuration = "namedElements"))
}