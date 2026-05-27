package bke.iso.engine.pathfinding

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class PathingType {
    WALKABLE,
    BLOCKER,
    IGNORE
}

@Serializable
@SerialName("pathing")
data class Pathing(
    @SerialName("pathingType")
    var type: PathingType = PathingType.WALKABLE
) : Component
