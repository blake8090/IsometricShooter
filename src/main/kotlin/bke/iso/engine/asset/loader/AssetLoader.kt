package bke.iso.engine.asset.loader

import java.io.File

interface AssetLoader<T : Any> {
    /**
     * List of file extensions (without the '.') that the AssetLoader will load.
     */
    val extensions: List<String>

    fun load(file: File): T
}
