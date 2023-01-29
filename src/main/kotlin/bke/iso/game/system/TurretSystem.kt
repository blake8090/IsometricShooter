package bke.iso.game.system

import bke.iso.service.Transient
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.event.EventService
import bke.iso.engine.math.toVector2
import bke.iso.engine.physics.CollisionService
import bke.iso.engine.render.DebugRenderService
import bke.iso.engine.system.System
import bke.iso.game.Player
import bke.iso.game.Turret
import bke.iso.game.event.BulletType
import bke.iso.game.event.ShootEvent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.max

@Transient
class TurretSystem(
    private val entityService: EntityService,
    private val collisionService: CollisionService,
    private val eventService: EventService,
    private val debugRenderService: DebugRenderService
) : System {
    private val visionRadius = 12f
    private val coolDownSeconds = 0.5f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Turret::class) { entity, turret ->
            debugRenderService.addCircle(Vector3(entity.x, entity.y, entity.z), visionRadius, Color.GOLD)

            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)

            val target = findTarget(entity)
            if (turret.coolDownTime == 0f && target != null) {
                eventService.fire(ShootEvent(entity, target, BulletType.TURRET))
                turret.coolDownTime = coolDownSeconds
            }
        }
    }

    private fun findTarget(turretEntity: Entity): Vector3? {
        val playerEntity = entityService.search.firstHavingComponent(Player::class) ?: return null

        val pos = Vector3(turretEntity.x, turretEntity.y, 0f)
        val playerPos = Vector3(playerEntity.x, playerEntity.y, 0f)

        // TODO: use Sphere instead
        if (!Circle(pos.toVector2(), visionRadius).contains(playerPos.toVector2())) {
            return null
        }

        debugRenderService.addLine(pos, playerPos, 1f, Color.RED)

        val turretToPlayer = Segment(
            pos,
            playerPos
        )
        val collision = collisionService.checkSegmentCollisions(turretToPlayer)
            .firstOrNull { turretEntity != it.data.entity }
            ?: return null

        for (point in collisionService.findIntersectionPoints(turretToPlayer, collision.data.box)) {
            debugRenderService.addPoint(Vector3(point), 2f, Color.YELLOW)
        }

        return if (playerEntity == collision.data.entity) {
            playerPos
        } else {
            null
        }
    }
}
