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
    withJavadocJar()
}

// Declare capabilities on the outgoing configurations.
// Read more about capabilities here: https://docs.gradle.org/current/userguide/component_capabilities.html#sec:declaring-additional-capabilities-for-a-local-component
setOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.getByName(variant).outgoing {
        capability("com.noxcrew:noxesium-${project.name}:$version")
        capability("com.noxcrew.noxesium:noxesium:$version")
    }
}

tasks {
    val processResourcesTasks = listOf("processResources")

    jar {
        from("LICENSE") {
            rename { return@rename "${it}_${rootProject.name}" }
        }
    }

    withType<AbstractArchiveTask> {
        archiveBaseName.set("noxesium-${project.name}")
    }

    withType<ProcessResources>().matching { processResourcesTasks.contains(it.name) }.configureEach {
        inputs.property("version", project.version)
        filesMatching(setOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
            expand("version" to project.version)
        }
    }
}