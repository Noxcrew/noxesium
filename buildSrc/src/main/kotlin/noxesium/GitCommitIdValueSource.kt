package noxesium

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/** Provides the git commit id from an external source. */
abstract class GitCommitIdValueSource
@Inject constructor(
    private val execOperations: ExecOperations,
) : ValueSource<String, GitCommitIdValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val rootDirectory: DirectoryProperty
    }

    override fun obtain(): String = ByteArrayOutputStream().use { stream ->
        execOperations.exec {
            workingDir(parameters.rootDirectory.get())
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stream
        }
        stream.toString().trim().filterNot(Char::isWhitespace)
    }
}