package bke.iso

import java.io.File

/**
 * Wrapper for a [File], allowing easier unit testing of services
 */
class FilePointer(private val file: File) {
    fun getPath(): String = file.path
    fun getExtension() = file.extension
    fun getNameWithoutExtension() = file.nameWithoutExtension
    fun isDirectory() = file.isDirectory
    // TODO: Rename this to something better
    fun getFile() = file
}

@Service
class FileService {
    fun getFile(path: String): FilePointer =
        FilePointer(File(path))
}
