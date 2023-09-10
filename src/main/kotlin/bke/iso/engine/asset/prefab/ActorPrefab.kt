package bke.iso.engine.asset.prefab

import bke.iso.engine.asset.AssetLoader
import bke.iso.engine.world.Component
import java.io.File

data class ActorPrefab(
    val name: String,
    val components: List<Component>
)

class ActorPrefabLoader : AssetLoader<ActorPrefab> {

    override fun load(file: File): ActorPrefab {
        TODO("Not yet implemented")
    }
}
