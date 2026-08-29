package bke.iso.engine.physics

import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val forces: MutableList<@Contextual Vector3> = mutableListOf(),
) : Component {

    @Contextual
    @Transient
    val pendingImpulse: Vector3 = Vector3()

    /**
     * Queues a one-time impulse. Physics converts it to velocity using `impulse / mass`,
     * so heavier bodies receive a smaller velocity change.
     */
    fun applyImpulse(impulse: Vector3) {
        pendingImpulse.add(impulse)
    }

    /**
     * Queues a direct velocity change independent of mass, useful for effects such as jumping
     * that should behave consistently across bodies.
     */
    fun applyVelocityChange(deltaVelocity: Vector3) {
        pendingImpulse.mulAdd(deltaVelocity, mass)
    }
}

data class GroundContact(
    var support: Entity
) : Component
