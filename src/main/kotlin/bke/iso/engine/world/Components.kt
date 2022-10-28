package bke.iso.engine.world

import com.badlogic.gdx.math.Vector2

open class Component

data class Sprite(
    val texture: String,
    val offset: Vector2 = Vector2()
) : Component()
