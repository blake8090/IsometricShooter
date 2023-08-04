package bke.iso.v2.engine.render

import bke.iso.v2.engine.world.Component

data class Sprite(
    val texture: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Component()
