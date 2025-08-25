plugins {
    id("net.neoforged.moddev")
}

/*
    Preferably, this would use Loom over NeoForm but Loom does not support not
    remapping outputs without using the `namedElements` configuration, a detail
    that is not kept by Maven so any external dependencies wouldn't be able to
    properly load the NMS sources.

    Paperweight is also not an option because it ignores client-only classes.
 */

neoForge {
    setNeoFormVersion("${property("neoform_version")}")
}

dependencies {
    compileOnly(project(":api"))
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