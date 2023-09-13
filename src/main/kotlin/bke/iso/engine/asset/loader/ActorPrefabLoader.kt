package bke.iso.engine.asset.loader

import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
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
