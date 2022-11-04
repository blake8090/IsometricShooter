package bke.iso.engine.entity

import com.badlogic.gdx.math.Vector2

/**
 * Contains all common components that don't belong to a particular system.
 */

data class Sprite(
    val texture: String,
    val offset: Vector2 = Vector2()
) : Component()
