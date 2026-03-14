plugins {
    id("net.fabricmc.fabric-loom")
    id("noxesium.publishing")
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(project(":api"))
    compileOnly(libs.prtree)
}

configurations {
    register("nmsJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("nmsJava", sourceSets["main"].java.sourceDirectories.singleFile)
}