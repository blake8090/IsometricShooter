package bke.iso.engine.physics

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

data class Velocity(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) : Component()

data class Acceleration(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) : Component()

data class Gravity(
    val acceleration: Float = GRAVITY_ACCELERATION,
    val terminalVelocity: Float = TERMINAL_VELOCITY
) : Component()
