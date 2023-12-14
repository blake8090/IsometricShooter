package bke.iso.game.player

import bke.iso.engine.world.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class PlayerState {
    STAND,
    CROUCH,
    NONE
}

@Serializable
@SerialName("player")
data class Player(
    var state: PlayerState = PlayerState.NONE
) : Component
