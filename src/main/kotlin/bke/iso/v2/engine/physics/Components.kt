package bke.iso.v2.engine.physics

import bke.iso.v2.engine.entity.Component

data class Bounds(
    val width: Float,
    val length: Float,
    val offsetX: Float,
    val offsetY: Float
)

data class Collision(
    val bounds: Bounds,
    val solid: Boolean
) : Component()

data class Velocity(
    val x: Float,
    val y: Float,
    val speed: Float
) : Component()
