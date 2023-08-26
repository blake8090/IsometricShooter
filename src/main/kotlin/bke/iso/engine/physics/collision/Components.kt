package bke.iso.engine.physics.collision

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3

data class Collider (
    val solid: Boolean,
    val size: Vector3,
    val offset: Vector3 = Vector3()
) : Component()

data class FrameCollisions(
    val collisions: MutableSet<Collision> = mutableSetOf()
) : Component()
