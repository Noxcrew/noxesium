import gradle.kotlin.dsl.accessors._38abe6feebbbb2ba8fa777a7b88e8035.jar
import gradle.kotlin.dsl.accessors._38abe6feebbbb2ba8fa777a7b88e8035.java
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
}