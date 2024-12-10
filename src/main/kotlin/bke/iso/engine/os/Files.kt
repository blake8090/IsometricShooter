package bke.iso.engine.os

import bke.iso.engine.GameInfo
import org.apache.commons.lang3.SystemUtils
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

    /**
     * Returns the user's home directory as a string.
     */
    fun getHomeDirectory(): String {
        return System.getProperty("user.home")
    }

    /**
     * Returns the path where game data such as saves, logs and configuration should be saved.
     * The returned path differs depending on the user's OS.
     */
    fun getGameDataPath(gameInfo: GameInfo): String {
        return if (SystemUtils.IS_OS_WINDOWS) {
            "${System.getenv("LOCALAPPDATA")}\\${gameInfo.folderName}"
        } else {
            "${getHomeDirectory()}/${gameInfo.folderName}"
        }
    }
}
