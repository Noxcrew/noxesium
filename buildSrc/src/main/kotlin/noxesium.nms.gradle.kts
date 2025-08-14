plugins {
    `java-library`
}

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
}

dependencies {
    api(project(":nms"))
    "commonJava"(project(":nms", "commonJava"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("commonJava"))
        source(configurations.getByName("commonJava"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("commonJava"))
        from(configurations.getByName("commonJava"))
    }
}