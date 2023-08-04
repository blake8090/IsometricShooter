package bke.iso.v2.engine.physics

import bke.iso.v2.engine.world.Component
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
    val collisions: MutableSet<Collision> = mutableSetOf()
) : Component()

data class Velocity(
    val delta: Vector3 = Vector3(),
    val speed: Vector3 = Vector3()
) : Component()
