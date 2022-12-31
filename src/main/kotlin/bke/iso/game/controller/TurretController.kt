package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.Engine
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.CollisionService
import bke.iso.engine.render.DebugCircle
import bke.iso.engine.render.DebugData
import bke.iso.engine.render.DebugLine
import bke.iso.engine.render.DebugPoint
import bke.iso.game.Player
import bke.iso.game.Turret
import bke.iso.game.event.BulletType
import bke.iso.game.event.ShootEvent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.max

class TurretController(
    private val entityService: EntityService,
    private val collisionService: CollisionService,
    private val engine: Engine
) : Controller {
    private val visionRadius = 4f
    private val coolDownSeconds = 3f

    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Turret::class) { entity, turret ->
            val debugData = getDebugData(entity)
            debugData.circles.add(DebugCircle(visionRadius, Color.GOLD))

            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)

            val target = findTarget(entity)
            if (turret.coolDownTime == 0f && target != null) {
                engine.fireEvent(ShootEvent(entity, target, BulletType.TURRET))
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
