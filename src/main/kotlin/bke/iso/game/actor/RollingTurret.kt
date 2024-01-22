package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@SerialName("rollingTurret")
data class RollingTurret(
    var moving: Boolean = false
) : Component

private const val MOVE_SPEED = 2f

class RollingTurretSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actors.each(RollingTurret::class, ::update)
    }

    private fun update(actor: Actor, rollingTurret: RollingTurret) {
        if (!rollingTurret.moving) {
            startMoving(actor)
            rollingTurret.moving = true
        }
    }

    private fun startMoving(actor: Actor) {
        val vx =
            if (nextFloat(0f, 1f) < 0.5f) {
                MOVE_SPEED * -1f
            } else {
                MOVE_SPEED
            }

        val vy =
            if (nextFloat(0f, 1f) < 0.5f) {
                MOVE_SPEED * -1f
            } else {
                MOVE_SPEED
            }

        actor.get<PhysicsBody>()
            ?.velocity
            ?.set(vx, vy, 0f)
    }

    private fun nextFloat(min: Float, max: Float) =
        Random.nextFloat() * (max - min) + min
}
