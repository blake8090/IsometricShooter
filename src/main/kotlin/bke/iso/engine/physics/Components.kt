package bke.iso.engine.physics

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3

data class Motion(
    val velocity: Vector3 = Vector3(),
    val acceleration: Vector3 = Vector3()
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
 * Defines an actor's movement behavior as well as interactions with Colliders.
 *
 * @property DYNAMIC Responds to both gravity and impulses.
 * @property KINEMATIC Does not respond to gravity or impulses.
 * When colliding with a dynamic object, the object will always be pushed away using an impulse.
 * @property SOLID Does not move. Default body type for tiles and actors without a [PhysicsBody].
 * @property GHOST Does not respond to gravity or impulses, and collisions are ignored.
 */
enum class BodyType {
    DYNAMIC,
    KINEMATIC,
    SOLID,
    GHOST
}

data class PhysicsBody(val bodyType: BodyType) : Component()
