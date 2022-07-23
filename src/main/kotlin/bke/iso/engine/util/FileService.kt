package bke.iso.engine.util

import bke.iso.engine.di.Singleton
import java.io.File

class FilePointer(private val file: File) {
    fun getPath(): String = file.path
    fun getExtension() = file.extension
    fun getNameWithoutExtension() = file.nameWithoutExtension
    fun getRawFile() = file
    fun readText() = file.readText()
}

@Singleton
class FileService {
    /**
     * Returns a list of all files in the given path, including subdirectories
     */
    fun getFiles(path: String) =
        File(path)
            .walkTopDown()
            .map(::FilePointer)
            .toList()
}
