plugins {
    id("fabric-loom")
    id("noxesium.sync")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    implementation(project(path = ":fabric", configuration = "namedElements"))
}