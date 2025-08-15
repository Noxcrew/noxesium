plugins {
    `java-library`
}

configurations {
    register("nmsJava") {
        isCanBeResolved = true
    }
}

dependencies {
    api(project(":nms"))
    "nmsJava"(project(":nms", "nmsJava"))
}

tasks {
    named<JavaCompile>("compileJava").configure {
        dependsOn(configurations.getByName("nmsJava"))
        source(configurations.getByName("nmsJava"))
    }
    named<Jar>("sourcesJar").configure {
        dependsOn(configurations.getByName("nmsJava"))
        from(configurations.getByName("nmsJava"))
    }
}