package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Component
import com.badlogic.gdx.math.Vector3

data class Bounds(
    val dimensions: Vector3,
    val offset: Vector3 = Vector3()
)

data class Collider(
    val bounds: Bounds,
    val solid: Boolean
) : Component()

data class FrameCollisions(
    val collisions: MutableSet<ObjectCollision> = mutableSetOf()
) : Component()
