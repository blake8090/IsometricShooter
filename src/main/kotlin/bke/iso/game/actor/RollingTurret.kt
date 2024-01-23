package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.game.weapon.Inventory
import bke.iso.game.weapon.WeaponsModule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.random.Random

@Serializable
@SerialName("rollingTurret")
data class RollingTurret(
    var moving: Boolean = false,
    var timer: Float = 0f,
    var state: RollingTurretState = RollingTurretState.MOVE
) : Component

enum class RollingTurretState(val durationSeconds: Float) {
    MOVE(3f),
    SHOOT(1f)
}

private const val MOVE_SPEED = 2f

class RollingTurretSystem(
    private val world: World,
    private val collisions: Collisions,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, rollingTurret: RollingTurret ->
            if (!actor.has<Inventory>()) {
                weaponsModule.equip(actor, "turret")
            }
            update(actor, rollingTurret, deltaTime)
        }
    }

    private fun update(actor: Actor, rollingTurret: RollingTurret, deltaTime: Float) {
        rollingTurret.timer = max(0f, rollingTurret.timer - deltaTime)

        if (rollingTurret.timer == 0f) {
            when (rollingTurret.state) {
                RollingTurretState.MOVE -> setShootState(actor, rollingTurret)
                RollingTurretState.SHOOT -> setMoveState(actor, rollingTurret)
            }
        } else {
            when (rollingTurret.state) {
                RollingTurretState.MOVE -> runMoveState(actor)
                RollingTurretState.SHOOT -> runShootState(rollingTurret)
            }
        }
    }

    private fun setMoveState(actor: Actor, rollingTurret: RollingTurret) {
        rollingTurret.state = RollingTurretState.MOVE
        rollingTurret.timer = RollingTurretState.MOVE.durationSeconds

        startMoving(actor)
        rollingTurret.moving = true
    }

    private fun setShootState(actor: Actor, rollingTurret: RollingTurret) {
        rollingTurret.state = RollingTurretState.SHOOT
        rollingTurret.timer = RollingTurretState.SHOOT.durationSeconds

        actor.with<PhysicsBody> { physicsBody ->
            physicsBody.velocity.x = 0f
            physicsBody.velocity.y = 0f
        }

        rollingTurret.moving = false
    }

    private fun runMoveState(actor: Actor) {
        val collision = collisions
            .getCollisions(actor)
            .firstOrNull(::validCollision)

        if (collision != null) {
            startMoving(actor)
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

        actor.with<PhysicsBody> { physicsBody ->
            physicsBody.velocity.x = vx
            physicsBody.velocity.y = vy
        }
    }

    private fun validCollision(collision: Collision): Boolean {
        if (collision.obj is Tile) {
            return false
        }

        if (collision.side == CollisionSide.TOP || collision.side == CollisionSide.BOTTOM) {
            return false
        }

        return true
    }

    private fun nextFloat(min: Float, max: Float) =
        Random.nextFloat() * (max - min) + min

    private fun runShootState(rollingTurret: RollingTurret) {
    }
}
