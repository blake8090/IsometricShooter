package bke.iso.engine.physics

import bke.iso.engine.entity.Component
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

data class Velocity(
    val dx: Float = 0f,
    val dy: Float = 0f
) : Component()

data class CollisionBounds(
    val width: Float,
    val length: Float,
    val offset: Vector2 = Vector2()
)

data class Collision(
    val bounds: CollisionBounds,
    val solid: Boolean = false
) : Component()

/**
 * Contains a rectangle defining the entity's projected collision area.
 *
 * The projected collision area is relative to the world, and includes velocity changes.
 */
data class CollisionProjection(
    val xProjection: Rectangle? = null,
    val yProjection: Rectangle? = null
) : Component()