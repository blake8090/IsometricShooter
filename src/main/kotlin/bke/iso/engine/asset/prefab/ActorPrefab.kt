package bke.iso.engine.asset.prefab

import bke.iso.engine.Serializer
import bke.iso.engine.asset.loader.AssetLoader
import bke.iso.engine.world.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ActorPrefab(
    val name: String,
    val components: List<Component>
)

class ActorPrefabLoader(private val serializer: Serializer) : AssetLoader<ActorPrefab> {

    override val extensions: List<String> = listOf("actor")

    override suspend fun load(file: File): ActorPrefab =
        withContext(Dispatchers.IO) {
            serializer.read<ActorPrefab>(file.readText())
        }
}
