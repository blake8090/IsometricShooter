package bke.iso.v2.engine.file

import java.io.File

class FileSystem {
    /**
     * Returns a list of all files in the given path, including subdirectories.
     * Given path is relative to the current working directory.
     */
    fun getFiles(path: String): List<File> =
        File(path)
            .walkTopDown()
            .filter(File::isFile)
            .toList()
}
