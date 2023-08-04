package bke.iso.engine.render

import bke.iso.engine.world.Component

data class Sprite(
    val texture: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Component()
