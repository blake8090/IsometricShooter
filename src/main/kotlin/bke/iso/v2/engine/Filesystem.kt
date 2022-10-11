package bke.iso.v2.engine

import bke.iso.v2.app.service.Service
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

@Service
class Filesystem {
    /**
     * Returns a list of all files in the given path, including subdirectories
     */
    fun getFiles(path: String) =
        File(path)
            .walkTopDown()
            .map(::FilePointer)
            .toList()
}
