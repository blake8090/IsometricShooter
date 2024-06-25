package bke.iso.game.elevator

import bke.iso.engine.world.actor.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("elevator")
class Elevator(val speed: Float) : Component

@Serializable
@SerialName("elevatorTask")
data class ElevatorTask(
    val targetZ: Float,
    val direction: ElevatorDirection
) : Component

enum class ElevatorDirection {
    UP,
    DOWN
}
