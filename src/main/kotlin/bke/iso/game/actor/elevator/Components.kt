package bke.iso.game.actor.elevator

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("elevator")
class Elevator(val speed: Float = 0f) : Component

@Serializable
@SerialName("elevatorTask")
data class ElevatorTask(
    val targetZ: Float = 0f,
    val direction: ElevatorDirection = ElevatorDirection.UP
) : Component

enum class ElevatorDirection {
    UP,
    DOWN
}
