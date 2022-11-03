package bke.iso.engine.world.entity

import com.badlogic.gdx.math.Vector2

open class Component

data class Sprite(
    val texture: String,
    val offset: Vector2 = Vector2()
) : Component()

/**
 * Defines a collision box as a 2D rectangle.
 * @param x x position of the bottom-left corner of the box, relative to an entity's origin
 * @param y y position of the bottom-left corner of the box, relative to an entity's origin
 * @param width width of the box
 * @param length length of the box
 */
data class CollisionBox(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float,
    val length: Float
) : Component()
