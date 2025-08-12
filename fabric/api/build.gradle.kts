plugins {
    id("fabric-loom")
    id("noxesium.common")
}

loom {
    accessWidenerPath.set(file("src/main/resources/noxesium.accesswidener"))

    mixin {
        defaultRefmapName.set("noxesium.refmap.json")
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    // Compatibility with other mods
    if (property("enableSodium") == "true") {
        modImplementation(libs.sodium)
    }
    if (property("enableModMenu") == "true") {
        modImplementation(libs.modmenu) {
            isTransitive = false
        }
    }

    // Compile against the API project
    compileOnlyApi(project(":api"))
}