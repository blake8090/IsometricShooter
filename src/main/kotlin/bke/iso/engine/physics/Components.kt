package bke.iso.engine.physics

import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Defines an entity's movement behavior as well as interactions with Colliders.
 *
 * @property DYNAMIC Responds to both gravity and impulses.
 * @property KINEMATIC Does not respond to gravity or impulses.
 * When colliding with a dynamic object, the object will always be pushed away using an impulse.
 * @property SOLID Does not move. Default body type for tiles and entities without a [PhysicsBody].
 * @property GHOST Does not respond to gravity or impulses, and collisions are ignored.
 */
enum class PhysicsMode {
    DYNAMIC,
    KINEMATIC,
    SOLID,
    GHOST
}

@Serializable
@SerialName("physicsBody")
data class PhysicsBody(
    val mode: PhysicsMode = PhysicsMode.SOLID,
    @Contextual
    val velocity: Vector3 = Vector3(),
    val mass: Float = 1f,
    val forces: MutableList<@Contextual Vector3> = mutableListOf()
) : Component
