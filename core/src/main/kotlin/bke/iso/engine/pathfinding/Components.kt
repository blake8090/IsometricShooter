package bke.iso.engine.pathfinding

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
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

// TODO: finish adding navigation
@Serializable
@SerialName("navigationAgent")
data class NavigationAgent(
    var navProfile: String = "default",
    @Contextual
    var targetPosition: Vector3? = null,
    var pathDesiredDistance: Float = 0.35f,
    var targetDesiredDistance: Float = 0.25f,
    var pathMaxDistance: Float = 1.0f
) : Component
