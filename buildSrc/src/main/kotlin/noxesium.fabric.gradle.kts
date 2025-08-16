plugins {
    `java-library`
}

tasks {
    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)
        filesMatching(setOf("fabric.mod.json")) {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        // Exclude sodium java classes when it's disabled
        if (rootProject.property("enableSodium") != "true") {
            exclude("**/sodium/**.java")
        }
        if (rootProject.property("enableModMenu") != "true") {
            exclude("**/modmenu/**.java")
        }
    }
}