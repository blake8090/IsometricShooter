package bke.iso.engine.asset.shader

import bke.iso.engine.asset.AssetCache
import java.io.File

data class ShaderFile(
    val fileName: String,
    val content: String
)

class ShaderFileAssetCache : AssetCache<ShaderFile>() {
    override val extensions: Set<String> = setOf("glsl")

    override suspend fun load(file: File) {
        val asset = ShaderFile(file.name, file.readText())
        store(file, file.name, asset)
    }
}
