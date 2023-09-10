package bke.iso.game.combat

import bke.iso.engine.world.Component
import bke.iso.engine.world.ComponentSubType

@ComponentSubType("health")
data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component()

@ComponentSubType("healthBar")
data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component()
