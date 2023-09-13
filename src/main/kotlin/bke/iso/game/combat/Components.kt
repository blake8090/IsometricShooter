package bke.iso.game.combat

import bke.iso.engine.world.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("health")
data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component

@Serializable
@SerialName("healthBar")
data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component
