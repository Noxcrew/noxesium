plugins {
    `java-library`
}

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
}

dependencies {
    api(project(":example:example-common"))
    "commonJava"(project(":example:example-common", "commonJava"))
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