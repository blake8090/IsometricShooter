package bke.iso.old.engine.physics

import bke.iso.old.engine.entity.Component
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

data class Velocity(
    val delta: Vector3 = Vector3(),
    val speed: Vector3 = Vector3()
) : Component()
