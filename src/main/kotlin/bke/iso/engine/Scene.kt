package bke.iso.engine

import bke.iso.engine.world.Tile
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val version: String,
    val actors: List<ActorRecord>,
    val tiles: List<Tile>
)

@Serializable
data class ActorRecord(
    @Contextual
    val pos: Vector3,
    val prefab: String
)
