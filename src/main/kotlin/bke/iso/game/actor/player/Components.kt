package bke.iso.game.actor.player

import bke.iso.engine.world.actor.Component
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
