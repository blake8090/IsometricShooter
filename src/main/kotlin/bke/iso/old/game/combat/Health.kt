package bke.iso.old.game.combat

import bke.iso.old.engine.entity.Component

data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component()

data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component()
