package bke.iso.game.combat

import bke.iso.engine.world.Component
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("health")
data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component()

@JsonTypeName("healthBar")
data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component()
