package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.physics.getPhysicsMode
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import kotlin.random.Random

@Serializable
@SerialName("rollingTurret")
data class RollingTurret(
    var moving: Boolean = false
) : Component

class RollingTurretSystem(
    private val world: World,
    private val collisions: Collisions
) : System {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        world.actors.each(RollingTurret::class, ::update)
    }

    private fun update(actor: Actor, rollingTurret: RollingTurret) {
//        if (actor.z == 0f) {
//            actor.move(0f, 0f, 0.1f)
//        }

        if (!rollingTurret.moving) {
            startMoving(actor)
            rollingTurret.moving = true
        }

        val collisions = collisions.getCollisions(actor)
        val solidCollision = collisions
            .find { collision ->
                getPhysicsMode(collision.obj) == PhysicsMode.SOLID
                        && collision.side != CollisionSide.TOP
                        && collision.side != CollisionSide.BOTTOM
            }

        if (solidCollision != null) {
            log.debug { "collided with ${solidCollision.obj} on side ${solidCollision.side}" }
            startMoving(actor)
            bounce(actor, solidCollision)
        }
    }

    private fun bounce(actor: Actor, collision: Collision) {
//        when (collision.side) {
//            CollisionSide.BACK -> {
//
//            }
//        }
    }

    private fun startMoving(actor: Actor) {
        val direction = Vector3(
            nextFloat(-1f, 1f),
            nextFloat(-1f, 1f),
            0f
        )

        actor.get<PhysicsBody>()
            ?.velocity
            ?.set(direction)
            ?.scl(1.2f)
    }

    private fun nextFloat(min: Float, max: Float) =
        Random.nextFloat() * (max - min) + min
}
