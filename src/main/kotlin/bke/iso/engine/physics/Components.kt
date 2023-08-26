package bke.iso.engine.physics

import bke.iso.engine.world.Component

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
