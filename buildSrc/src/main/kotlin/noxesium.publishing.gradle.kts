plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven {
            name = "noxcrew-public"
            url = uri("https://maven.noxcrew.com/public")
            credentials {
                username = System.getenv("NOXCREW_MAVEN_PUBLIC_USERNAME")
                password = System.getenv("NOXCREW_MAVEN_PUBLIC_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            version = "${property("mod_version")}"
            from(components["java"])
        }
    }
}
