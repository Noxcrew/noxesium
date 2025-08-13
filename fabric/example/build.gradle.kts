plugins {
    id("fabric-loom")
}

loom {
    // This is not necessary to copy, it's just a workaround because this example module is in the same repository.
    // In general this gradle set-up is not a great example of how yours should look, just look at the code!
    enableTransitiveAccessWideners = false
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.modmenu) {
        isTransitive = false
    }
    implementation(project(path = ":fabric:fabric-common", configuration = "namedElements"))
}