package bke.iso.engine

import bke.iso.service.SingletonService
import java.io.File

/**
 * Provides a wrapper around a [File] instance, facilitating unit testing
 */
class FilePointer(private val file: File) {
    fun getPath(): String = file.path
    fun getExtension() = file.extension
    fun getNameWithoutExtension() = file.nameWithoutExtension
    fun getRawFile() = file
    fun readText() = file.readText()
}

class FileService : SingletonService {
    /**
     * Returns a list of all files in the given path, including subdirectories.
     * Given path is relative to the current working directory.
     */
    // TODO: error handling
    fun getFiles(path: String): List<FilePointer> =
        File(path)
            .walkTopDown()
            .filter(File::isFile)
            .map(::FilePointer)
            .toList()

    /**
     * Returns the full canonical path as a [String], relative to the program's current working directory.
     * @param path a relative path, e.g. `"assets"`
     * @return the full canonical path, e.g. `"C:/program/assets"`
     */
    fun getCanonicalPath(path: String): String =
        File(".", path)
            .canonicalPath
}
