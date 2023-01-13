package bke.iso.v2.game.system

import bke.iso.service.Transient
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.event.EventService
import bke.iso.v2.engine.physics.CollisionService
import bke.iso.v2.engine.render.DebugCircle
import bke.iso.v2.engine.render.DebugData
import bke.iso.v2.engine.render.DebugLine
import bke.iso.v2.engine.render.DebugPoint
import bke.iso.v2.engine.system.System
import bke.iso.v2.game.Player
import bke.iso.v2.game.Turret
import bke.iso.v2.game.event.BulletType
import bke.iso.v2.game.event.ShootEvent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.max

@Transient
class TurretSystem(
    private val entityService: EntityService,
    private val collisionService: CollisionService,
    private val eventService: EventService
) : System {
    private val visionRadius = 12f
    private val coolDownSeconds = 0.5f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Turret::class) { entity, turret ->
            val debugData = getDebugData(entity)
            debugData.circles.add(DebugCircle(visionRadius, Color.GOLD))

            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)

            val target = findTarget(entity)
            if (turret.coolDownTime == 0f && target != null) {
                eventService.fire(ShootEvent(entity, target, BulletType.TURRET))
                turret.coolDownTime = coolDownSeconds
            }
        }
    }

    private fun findTarget(turretEntity: Entity): Vector2? {
        val playerEntity = entityService.getAll()
            .firstOrNull { entity -> entity.has<Player>() }
            ?: return null

        val pos = Vector2(turretEntity.x, turretEntity.y)
        val playerPos = Vector2(playerEntity.x, playerEntity.y)

        if (!Circle(pos, visionRadius).contains(playerPos)) {
            return null
        }

        val debugData = getDebugData(turretEntity)
        debugData.lines.add(
            DebugLine(
                pos,
                playerPos,
                1f,
                Color.RED
            )
        )

        val turretToPlayer = Segment(
            Vector3(pos, 0f),
            Vector3(playerPos, 0f)
        )
        val collision = collisionService.checkSegmentCollisions(turretToPlayer)
            .firstOrNull { turretEntity != it.data.entity }
            ?: return null

        for (point in collisionService.findIntersectionPoints(turretToPlayer, collision.data.box)) {
            debugData.points.add(DebugPoint(point, 2f, Color.YELLOW))
        }

        return if (playerEntity == collision.data.entity) {
            playerPos
        } else {
            null
        }
    }

    private fun getDebugData(entity: Entity): DebugData {
        var debugData = entity.get<DebugData>()
        if (debugData == null) {
            debugData = DebugData()
            entity.add(debugData)
        }
        return debugData
    }
}
