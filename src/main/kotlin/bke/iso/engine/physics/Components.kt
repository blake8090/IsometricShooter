package bke.iso.engine.physics

import bke.iso.engine.entity.Component

data class Bounds(
    val width: Float,
    val length: Float,
    val height: Float,
    val offsetX: Float,
    val offsetY: Float
)

data class Collision(
    val bounds: Bounds,
    val solid: Boolean
) : Component()
