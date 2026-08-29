package bke.iso.engine.movement

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MovementMode {
    INSTANT,
    ACCELERATED
}

@Serializable
@SerialName("movementProperties")
data class MovementProperties(
    var maxSpeed: Float = 0f,
    var speedMultiplier: Float = 1f,
    var acceleration: Float = 0f,
    var deceleration: Float = acceleration,
    var mode: MovementMode = MovementMode.INSTANT
) : Component

data class MovementIntent(
    var direction: Vector3 = Vector3()
) : Component
