package bke.iso.engine.asset.cache

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

    override suspend fun loadAssets(file: File): List<LoadedAsset<TilePrefab>> =
        withContext(Dispatchers.IO) {
            serializer.read<List<TilePrefab>>(file.readText())
                .map { prefab -> LoadedAsset(prefab.name, prefab) }
                .toList()
        }
}
