package bke.iso.engine.asset.entity

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.render.Sprite
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class TileTemplate(
    val name: String,
    val sprite: Sprite,
    val solid: Boolean
)

class TileTemplateAssetCache(private val serializer: Serializer) : AssetCache<TileTemplate>() {
    override val extensions: Set<String> = setOf("tiles")

    override suspend fun load(file: File) {
        withContext(Dispatchers.IO) {
            val templates = serializer.read<List<TileTemplate>>(file.readText())
            for (template in templates) {
                store(file, template.name, template)
            }
        }
    }
}
