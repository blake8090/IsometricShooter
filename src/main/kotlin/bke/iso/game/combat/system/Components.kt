package bke.iso.game.combat.system

import bke.iso.engine.world.actor.Component
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

@Serializable
@SerialName("healEffect")
data class HealEffect(
    val amountPerSecond: Float,
    val durationSeconds: Float,
    var elapsedTime: Float = 0f
) : Component

@Serializable
@SerialName("hitEffect")
data class HitEffect(
    val durationSeconds: Float,
    var elapsedTime: Float = 0f
) : Component
