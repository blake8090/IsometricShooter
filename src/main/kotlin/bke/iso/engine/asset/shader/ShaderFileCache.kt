package bke.iso.engine.asset.shader

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.asset.LoadedAsset
import java.io.File

data class ShaderFile(
    val fileName: String,
    val content: String
)

class ShaderFileCache : AssetCache<ShaderFile>() {
    override val extensions: Set<String> = setOf("glsl")

    override suspend fun loadAssets(file: File): List<LoadedAsset<ShaderFile>> {
        val asset = ShaderFile(file.name, file.readText())
        return listOf(LoadedAsset(file.name, asset))
    }
}
