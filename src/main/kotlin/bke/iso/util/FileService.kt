package bke.iso.util

import bke.iso.Service
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.dataformat.toml.TomlMapper
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

    fun getFile(path: String) = FilePointer(File(path))

    // TODO: error handling?
    inline fun <reified T : Any> readDataFile(file: FilePointer): T? {
        val contents = file.getRawFile()
            .readBytes()
            .toString(Charsets.UTF_8)

        return TomlMapper()
            .readValue(
                contents,
                object : TypeReference<T>() {}
            )
    }
}
