package bke.iso.game.combat

import bke.iso.engine.world.Component

data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component()

data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component()
