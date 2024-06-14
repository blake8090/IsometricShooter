package bke.iso.engine.asset.prefab

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.render.Sprite
import bke.iso.engine.serialization.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class TilePrefab(
    val name: String,
    val sprite: Sprite,
    val solid: Boolean
)

class TilePrefabCache(private val serializer: Serializer) : AssetCache<TilePrefab>() {
    override val extensions: Set<String> = setOf("tiles")

    override suspend fun load(file: File) {
        withContext(Dispatchers.IO) {
            val prefabs = serializer.read<List<TilePrefab>>(file.readText())
            for (prefab in prefabs) {
                store(file, prefab.name, prefab)
            }
        }
    }
}
