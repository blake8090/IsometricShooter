package bke.iso.game.actor.player

import bke.iso.engine.asset.config.Config
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("playerConfig")
data class PlayerConfig(
    val jumpForce: Float,
    val baseMovementSpeed: Float,
    val crouchSpeedModifier: Float,
    val runSpeedModifier: Float
) : Config
