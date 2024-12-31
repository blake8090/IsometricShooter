package bke.iso.engine.asset.shader

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.serialization.Serializer
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ShaderInfo(
    val name: String,
    val fragmentShader: String,
    val vertexShader: String
)

class ShaderInfoAssetCache(private val serializer: Serializer) : AssetCache<ShaderInfo>() {
    override val extensions: Set<String> = setOf("shader")

    override suspend fun load(file: File) {
        val shaderInfo = serializer.read<ShaderInfo>(file.readText())
        store(file, file.name, shaderInfo)
    }
}
