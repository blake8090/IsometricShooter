package bke.iso.engine.scene

import bke.iso.engine.math.Location
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val version: String,
    val actors: List<ActorRecord>,
    val tiles: List<TileRecord>,
    @Contextual
    val backgroundColor: Color? = null
)

@Serializable
data class ActorRecord(
    @Contextual
    val pos: Vector3,
    val prefab: String,
    val building: String? = null,
    val componentOverrides: List<Component> = mutableListOf()
)

@Serializable
data class TileRecord(
    val location: Location,
    val prefab: String,
    val building: String? = null
)
