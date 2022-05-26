package bke.iso.util

import bke.iso.Service
import java.io.File

class FilePointer(private val file: File) {
    fun getPath(): String = file.path
    fun getExtension() = file.extension
    fun getNameWithoutExtension() = file.nameWithoutExtension
    fun getRawFile() = file
}

@Service
class FileService {
    fun getFiles(path: String) =
        File(path)
            .walkTopDown()
            .map(::FilePointer)
            .toList()
}
