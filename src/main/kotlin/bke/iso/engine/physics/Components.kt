package bke.iso.engine.physics

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3

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

data class Motion(
    val acceleration: Vector3 = Vector3(),
    val velocity: Vector3 = Vector3()
) : Component()

/**
 * Applied to an actor's velocity only once.
 */
data class Impulse(
    val x: Float,
    val y: Float,
    val z: Float
) : Component()

/**
 * Defines an actor's movement behavior as well as interactions with [BoxCollider]s.
 *
 * @property DYNAMIC Responds to both gravity and impulses.
 * @property KINEMATIC Does not respond to gravity or impulses.
 * When colliding with a dynamic object, the object will always be pushed away using an impulse.
 */
enum class BodyType {
    DYNAMIC,
    KINEMATIC
}

data class PhysicsBody(
    val bodyType: BodyType,
    var gravityScale: Float = 1f,
) : Component()

data class BoxCollider(
    val size: Vector3,
    val offset: Vector3 = Vector3()
) : Component()
