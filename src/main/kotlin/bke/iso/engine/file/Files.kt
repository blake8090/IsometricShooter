package bke.iso.engine.file

import java.io.File
import kotlin.io.path.Path

class Files {
    /**
     * Returns a list of all files in the given path, including subdirectories.
     * Given path is relative to the current working directory.
     */
    fun listFiles(path: String): Sequence<File> =
        File(path)
            .walk()
            .filter(File::isFile)

    fun combinePaths(base: String, path: String, forwardSlash: Boolean = true): String {
        val combined = Path(base, path).toString()
        return if (forwardSlash) {
            combined.replace("\\", "/")
        } else {
            combined
        }
    }

    fun relativePath(base: String, file: File, forwardSlash: Boolean = true): String {
        val basePath = Path(base)
        val subPath = Path(file.path)
        val path = basePath
            .relativize(subPath)
            .toString()

        return if (forwardSlash) {
            path.replace("\\", "/")
        } else {
            path
        }
    }

    fun relativeParentPath(base: String, file: File, forwardSlash: Boolean = true): String {
        val basePath = Path(base)
        val subPath = Path(file.parent)
        val path = basePath
            .relativize(subPath)
            .toString()

        return if (forwardSlash) {
            path.replace("\\", "/")
        } else {
            path
        }
    }
}
