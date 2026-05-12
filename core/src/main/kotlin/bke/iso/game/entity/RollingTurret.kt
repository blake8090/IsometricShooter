package bke.iso.game.entity

import bke.iso.engine.core.Events
import bke.iso.engine.state.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.nextFloat
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Tile
import bke.iso.game.entity.player.Player
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.system.RangedWeaponOffset
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
    private val events: Events,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.entities.each { entity: Entity, rollingTurret: RollingTurret ->
            if (!entity.has<Inventory>()) {
                weaponsModule.equip(entity, "turret")
                entity.add(RangedWeaponOffset(0f, 0f, GUN_HEIGHT))
            }
            update(entity, rollingTurret, deltaTime)
        }
    }

    private fun update(entity: Entity, rollingTurret: RollingTurret, deltaTime: Float) {
        rollingTurret.timer = max(0f, rollingTurret.timer - deltaTime)

        if (rollingTurret.timer == 0f) {
            when (rollingTurret.state) {
                RollingTurretState.MOVE -> setShootState(entity, rollingTurret)
                RollingTurretState.SHOOT -> setMoveState(entity, rollingTurret)
            }
        } else {
            when (rollingTurret.state) {
                RollingTurretState.MOVE -> runMoveState(entity)
                RollingTurretState.SHOOT -> runShootState(entity)
            }
        }
    }

    private fun setMoveState(entity: Entity, rollingTurret: RollingTurret) {
        rollingTurret.state = RollingTurretState.MOVE
        rollingTurret.timer = RollingTurretState.MOVE.durationSeconds

        startMoving(entity)
        rollingTurret.moving = true
    }

    private fun setShootState(entity: Entity, rollingTurret: RollingTurret) {
        rollingTurret.state = RollingTurretState.SHOOT
        rollingTurret.timer = RollingTurretState.SHOOT.durationSeconds

        entity.with<PhysicsBody> { physicsBody ->
            physicsBody.velocity.x = 0f
            physicsBody.velocity.y = 0f
        }

        rollingTurret.moving = false
    }

    private fun runMoveState(entity: Entity) {
        val collision = collisions
            .getCollisions(entity)
            .firstOrNull(::validCollision)

        if (collision != null) {
            startMoving(entity)
        }
    }

    private fun startMoving(entity: Entity) {
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

        entity.with<PhysicsBody> { physicsBody ->
            physicsBody.velocity.x = vx
            physicsBody.velocity.y = vy
        }
    }

    private fun validCollision(collision: Collision): Boolean {
        if (collision.entity.has<Tile>()) {
            return false
        }

        if (collision.side == CollisionSide.TOP || collision.side == CollisionSide.BOTTOM) {
            return false
        }

        return true
    }

    private fun runShootState(turretEntity: Entity) {
        val playerEntity = world.entities.find<Player>() ?: return
        if (withinRange(turretEntity, playerEntity) && canSee(turretEntity, playerEntity)) {
            events.fire(WeaponsModule.ShootEvent(turretEntity, getTargetPos(playerEntity)))
        }

        val weapon = weaponsModule.getSelectedWeapon(turretEntity)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(turretEntity))
        }
    }

    private fun withinRange(entity: Entity, target: Entity) =
        entity.pos.dst(target.pos) <= RANGE_RADIUS

    private fun canSee(entity: Entity, target: Entity): Boolean {
        val start = entity.pos
        start.z += GUN_HEIGHT
        val end = getTargetPos(target)

        val firstCollision = collisions
            .checkLineCollisions(start, end)
            .filter { collision -> collision.entity != entity }
            .minByOrNull { collision -> collision.distanceStart }
            ?: return false

        val firstPoint = firstCollision
            .points
            .minBy { point -> start.dst(point) }
        renderer.debug.category("vision").addPoint(firstPoint, 3f, Color.RED)

        return if (firstCollision.entity == target) {
            renderer.debug.category("vision").addLine(start, end, 1f, Color.RED)
            true
        } else {
            renderer.debug.category("vision").addLine(start, firstPoint, 1f, Color.RED)
            false
        }
    }

    private fun getTargetPos(target: Entity): Vector3 {
        val pos = target.pos

        // aim for center mass!
        target.with<Collider> { collider ->
            pos.z += collider.size.z * 0.5f
        }

        return pos
    }
}
