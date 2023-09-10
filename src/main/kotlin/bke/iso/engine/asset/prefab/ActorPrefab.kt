package bke.iso.engine.asset.prefab

import bke.iso.engine.Serializer
import bke.iso.engine.asset.AssetLoader
import bke.iso.engine.world.Component
import java.io.File

data class ActorPrefab(
    val name: String,
    val components: List<Component>
)

class ActorPrefabLoader(private val serializer: Serializer) : AssetLoader<ActorPrefab> {

    override fun load(file: File): ActorPrefab =
        serializer.read(file.readText())
}
