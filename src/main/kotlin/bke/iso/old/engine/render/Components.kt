package bke.iso.old.engine.render

import bke.iso.old.engine.entity.Component

data class Sprite(
    val texture: String = "",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Component()

class DrawShadow : Component()
