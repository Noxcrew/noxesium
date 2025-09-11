plugins {
    id("net.neoforged.moddev")
}

neoForge {
    setNeoFormVersion("${property("neoform_version")}")
}

dependencies {
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