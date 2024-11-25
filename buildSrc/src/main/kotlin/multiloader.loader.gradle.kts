plugins {
    id("multiloader.common")
}

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
    register("commonResources") {
        isCanBeResolved = true
    }
}

dependencies {
    api(project(":common"))
    "commonJava"(project(":common", "commonJava"))
    "commonResources"(project(":common", "commonResources"))
}

tasks {
    // Janky task to just copy over the resources folder
    val copyResourcesFromCommon by tasks.registering(Copy::class) {
        from(project(":common").file("src/main/resources"))
        into("src/main/resources")
        include("**/*")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<ProcessResources>("processResources").configure {
        dependsOn(copyResourcesFromCommon)
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("commonJava"))
        from(configurations.getByName("commonJava"))
        dependsOn(configurations.getByName("commonResources"))
        from(configurations.getByName("commonResources"))
    }
}