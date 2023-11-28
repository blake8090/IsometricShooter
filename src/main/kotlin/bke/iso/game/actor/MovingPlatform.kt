package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.World
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("movingPlatform")
data class MovingPlatform(
    val speed: Float = 1f,
    val maxZ: Float = 2f,
    val minZ: Float = 0f,
    val pauseSeconds: Float = 1f,
    var movingUp: Boolean = true
) : Component

class MovingPlatformSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, movingPlatform: MovingPlatform ->
            if (actor.z >= movingPlatform.maxZ) {
                actor.moveTo(actor.x, actor.y, movingPlatform.maxZ)
                movingPlatform.movingUp = movingPlatform.movingUp.not()
            } else if (actor.z <= movingPlatform.minZ) {
                actor.moveTo(actor.x, actor.y, movingPlatform.minZ)
                movingPlatform.movingUp = movingPlatform.movingUp.not()
            }

            val dir = if (movingPlatform.movingUp) 1f else -1f

            val body = checkNotNull(actor.get<PhysicsBody>()) {
                "Expected $actor to have a PhysicsBody"
            }
            body.velocity.z = dir * movingPlatform.speed
        }
    }
}
