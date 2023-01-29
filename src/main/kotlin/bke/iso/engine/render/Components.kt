package bke.iso.engine.render

import bke.iso.engine.entity.Component

data class Sprite(
    val texture: String,
    val offsetX: Float,
    val offsetY: Float
) : Component()
