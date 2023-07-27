import java.io.ByteArrayOutputStream

fun getGitCommit(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

allprojects {
    group = "com.noxcrew.noxesium"
    version = "${property("mod_version")}+${getGitCommit()}"
}
