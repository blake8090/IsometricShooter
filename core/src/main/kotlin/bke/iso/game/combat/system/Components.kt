package bke.iso.game.combat.system

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("health")
data class Health(
    val maxValue: Float = 0f,
    var value: Float = maxValue
) : Component

@Serializable
@SerialName("healthBar")
data class HealthBar(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Component

@Serializable
@SerialName("healEffect")
data class HealEffect(
    val amountPerSecond: Float = 0f,
    val durationSeconds: Float = 0f,
    var elapsedTime: Float = 0f
) : Component

@Serializable
@SerialName("hitEffect")
data class HitEffect(
    val durationSeconds: Float = 0f,
    var elapsedTime: Float = 0f
) : Component
