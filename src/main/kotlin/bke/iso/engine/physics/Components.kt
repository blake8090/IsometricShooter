package bke.iso.engine.physics

import bke.iso.engine.world.Component
import com.badlogic.gdx.math.Vector3
import com.fasterxml.jackson.annotation.JsonTypeName

/**
 * Defines an actor's movement behavior as well as interactions with Colliders.
 *
 * @property DYNAMIC Responds to both gravity and impulses.
 * @property KINEMATIC Does not respond to gravity or impulses.
 * When colliding with a dynamic object, the object will always be pushed away using an impulse.
 * @property SOLID Does not move. Default body type for tiles and actors without a [PhysicsBody].
 * @property GHOST Does not respond to gravity or impulses, and collisions are ignored.
 */
enum class PhysicsMode {
    DYNAMIC,
    KINEMATIC,
    SOLID,
    GHOST
}

@JsonTypeName("physicsBody")
data class PhysicsBody(
    val mode: PhysicsMode,
    val velocity: Vector3 = Vector3(),
    val mass: Float = 1f,
    val forces: MutableList<Vector3> = mutableListOf()
) : Component()
