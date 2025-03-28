import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.withType

plugins {
    idea
    `java-library`
    `maven-publish`
}

java {
    withSourcesJar()
}

tasks {
    jar {
        from("LICENSE") {
            rename { return@rename "${it}_${rootProject.name}" }
        }
    }

    withType<AbstractArchiveTask> {
        archiveBaseName.set("noxesium-${project.name}")
    }

    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)
        filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand("version" to project.version)
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()

        // Exclude sodium java classes when it's disabled
        if (rootProject.property("enableSodium") != "true") {
            exclude("**/sodium/**.java")
        }
        if (rootProject.property("enableModMenu") != "true") {
            exclude("**/modmenu/**.java")
        }
    }
}