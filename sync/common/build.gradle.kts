plugins {
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft(libs.minecraft)
    api(project(":api"))
    api(project(":nms"))
    api(libs.juniversalchardet)
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets["main"].java.sourceDirectories.singleFile)
}