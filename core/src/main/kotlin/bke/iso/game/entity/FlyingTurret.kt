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
import bke.iso.game.entity.player.Player
import bke.iso.game.weapon.system.Bullet
import bke.iso.game.weapon.system.RangedWeapon
import bke.iso.game.weapon.system.RangedWeaponOffset
import bke.iso.game.weapon.WeaponsModule
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
@SerialName("flyingTurret")
data class FlyingTurret(
    var targetHeight: Float? = null,
    var state: FlyingTurretState = FlyingTurretState.IDLE,
    var movementTimer: Float = 0f
) : Component

enum class FlyingTurretState {
    IDLE,
    CLIMB,
    WANDER
}

private const val HOVER_HEIGHT = 0.5f
private const val MOVE_SPEED = 0.7f
private const val MOVE_TIMER_SECONDS = 1f

private const val BOUNCE_RANGE = 0.1f
private const val BOUNCE_SPEED = 0.4f

private const val VISION_RADIUS = 8f

class FlyingTurretSystem(
    private val world: World,
    private val collisions: Collisions,
    private val renderer: Renderer,
    private val events: Events,
    private val weaponsModule: WeaponsModule
) : System {

    override fun update(deltaTime: Float) {
        world.entities.each<FlyingTurret> { entity, flyingTurret ->
            update(entity, flyingTurret, deltaTime)
        }
    }

    private fun update(entity: Entity, flyingTurret: FlyingTurret, deltaTime: Float) {
        when (flyingTurret.state) {
            FlyingTurretState.IDLE -> setClimbState(entity, flyingTurret)
            FlyingTurretState.CLIMB -> runClimbState(entity, flyingTurret)
            FlyingTurretState.WANDER -> runWanderState(entity, flyingTurret, deltaTime)
        }
    }

    private fun setClimbState(entity: Entity, flyingTurret: FlyingTurret) {
        flyingTurret.targetHeight = entity.z + HOVER_HEIGHT
        flyingTurret.state = FlyingTurretState.CLIMB
    }

    private fun runClimbState(entity: Entity, flyingTurret: FlyingTurret) {
        val targetHeight = checkNotNull(flyingTurret.targetHeight) {
            "Expected $entity to have a targetHeight"
        }

        val body = checkNotNull(entity.get<PhysicsBody>()) {
            "Expected $entity to have a PhysicsBody"
        }

        // snap to target height to avoid shimmering
        if (abs(targetHeight - entity.z) <= 0.01f) {
            entity.pos.z = targetHeight
            setWanderState(flyingTurret, body)
        } else if (entity.z > targetHeight) {
            body.velocity.z = -1f
        } else if (entity.z < targetHeight) {
            body.velocity.z = 1f
        }
    }

    private fun setWanderState(flyingTurret: FlyingTurret, body: PhysicsBody) {
        body.velocity.z = 1f
        flyingTurret.state = FlyingTurretState.WANDER
    }

    private fun runWanderState(entity: Entity, flyingTurret: FlyingTurret, deltaTime: Float) {
        flyingTurret.movementTimer += deltaTime

        val body = checkNotNull(entity.get<PhysicsBody>()) {
            "Expected $entity to have a PhysicsBody"
        }
        if (shouldStartMoving(flyingTurret, body)) {
            startMoving(body)
            flyingTurret.movementTimer = 0f
        }

        val collision = collisions
            .getCollisions(entity)
            .firstOrNull { collision -> !collision.entity.has<Bullet>() }

        if (collision != null) {
            bounce(body, collision)
        }

        val targetHeight = checkNotNull(flyingTurret.targetHeight) {
            "Expected $entity to have a targetHeight"
        }
        val high = targetHeight + BOUNCE_RANGE
        val low = targetHeight - BOUNCE_RANGE

        if (entity.z >= high) {
            body.velocity.z = BOUNCE_SPEED * -1f
        } else if (entity.z <= low) {
            body.velocity.z = BOUNCE_SPEED
        }

        findAndShootPlayer(entity)
    }

    private fun shouldStartMoving(flyingTurret: FlyingTurret, body: PhysicsBody): Boolean {
        if (body.velocity.x == 0f && body.velocity.y == 0f) {
            return true
        }
        return flyingTurret.movementTimer > MOVE_TIMER_SECONDS
    }

    private fun startMoving(physicsBody: PhysicsBody) {
        physicsBody.velocity.x =
            if (nextFloat(0f, 1f) < 0.5f) {
                MOVE_SPEED * -1f
            } else {
                MOVE_SPEED
            }

        physicsBody.velocity.y =
            if (nextFloat(0f, 1f) < 0.5f) {
                MOVE_SPEED * -1f
            } else {
                MOVE_SPEED
            }
    }

    private fun bounce(body: PhysicsBody, collision: Collision) {
        if (collision.side == CollisionSide.LEFT || collision.side == CollisionSide.RIGHT) {
            body.velocity.x *= -1f
        } else if (collision.side == CollisionSide.FRONT || collision.side == CollisionSide.BACK) {
            body.velocity.y *= -1f
        }
    }

    private fun findAndShootPlayer(entity: Entity) {
        val playerEntity = world.entities.find<Player>() ?: return
        if (canSee(entity, playerEntity)) {
            events.fire(WeaponsModule.ShootEvent(entity, getTargetPos(playerEntity)))
        }

        val weapon = weaponsModule.getSelectedWeapon(entity)
        if (weapon is RangedWeapon && weapon.ammo <= 0f) {
            events.fire(WeaponsModule.ReloadEvent(entity))
        }
    }

    private fun canSee(entity: Entity, target: Entity): Boolean {
        if (entity.pos.dst(target.pos) >= VISION_RADIUS) {
            return false
        }

        val start = entity.pos
        entity.with<RangedWeaponOffset> { offset ->
            start.z += offset.z
        }
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
