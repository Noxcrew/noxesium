plugins {
    id("fabric-loom")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.modmenu) {
        isTransitive = false
    }
    implementation(project(path = ":fabric:fabric-platform", configuration = "namedElements"))
}