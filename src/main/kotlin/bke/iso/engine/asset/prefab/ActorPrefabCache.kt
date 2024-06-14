package bke.iso.engine.asset.prefab

import bke.iso.engine.asset.AssetCache
import bke.iso.engine.asset.LoadedAsset
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.actor.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class ActorPrefab(
    val name: String,
    val components: List<Component>
)

class ActorPrefabCache(private val serializer: Serializer) : AssetCache<ActorPrefab>() {
    override val extensions: Set<String> = setOf("actor")

    override suspend fun loadAssets(file: File): List<LoadedAsset<ActorPrefab>> =
        withContext(Dispatchers.IO) {
            val prefab = serializer.read<ActorPrefab>(file.readText())
            listOf(LoadedAsset(prefab.name, prefab))
        }
}
