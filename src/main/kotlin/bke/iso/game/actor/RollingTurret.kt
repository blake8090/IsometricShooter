package bke.iso.game.actor

import bke.iso.engine.Game
import bke.iso.engine.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.nextFloat
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.game.actor.player.Player
import bke.iso.game.weapon.RangedWeapon
import bke.iso.game.weapon.RangedWeaponOffset
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
@SerialName("rollingTurret")
data class RollingTurret(
    var moving: Boolean = false,
    var timer: Float = 0f,
    var state: RollingTurretState = RollingTurretState.MOVE
) : Component

enum class RollingTurretState(val durationSeconds: Float) {
    MOVE(3f),
    SHOOT(0.75f)
}

private const val MOVE_SPEED = 1.5f
private const val RANGE_RADIUS = 8f
private const val GUN_HEIGHT = 0.25f

class RollingTurretSystem(
    private val world: World,
    private val collisions: Collisions,
    private val renderer: Renderer,
    private val events: Game.Events,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, rollingTurret: RollingTurret ->
            if (!actor.has<Inventory>()) {
                weaponsModule.equip(actor, "turret")
                actor.add(RangedWeaponOffset(0f, 0f, GUN_HEIGHT))
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
                RollingTurretState.SHOOT -> runShootState(actor)
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

    private fun runShootState(turretActor: Actor) {
        val playerActor = world.actors.find<Player>() ?: return
        if (withinRange(turretActor, playerActor) && canSee(turretActor, playerActor)) {
            events.fire(WeaponsModule.ShootEvent(turretActor, getTargetPos(playerActor)))
        }

        val weapon = weaponsModule.getSelectedWeapon(turretActor)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(turretActor))
        }
    }

    private fun withinRange(actor: Actor, target: Actor) =
        actor.pos.dst(target.pos) <= RANGE_RADIUS

    private fun canSee(actor: Actor, target: Actor): Boolean {
        val start = actor.pos
        start.z += GUN_HEIGHT
        val end = getTargetPos(target)

        val firstCollision = collisions
            .checkLineCollisions(start, end)
            .filter { collision -> collision.obj != actor }
            .minByOrNull { collision -> collision.distanceStart }
            ?: return false

        val firstPoint = firstCollision
            .points
            .minBy { point -> start.dst(point) }
        renderer.debug.category("vision").addPoint(firstPoint, 3f, Color.RED)

        return if (firstCollision.obj == target) {
            renderer.debug.category("vision").addLine(start, end, 1f, Color.RED)
            true
        } else {
            renderer.debug.category("vision").addLine(start, firstPoint, 1f, Color.RED)
            false
        }
    }

    private fun getTargetPos(target: Actor): Vector3 {
        val pos = target.pos

        // aim for center mass!
        target.with<Collider> { collider ->
            pos.z += collider.size.z * 0.5f
        }

        return pos
    }
}
