package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.Engine
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.CollisionService
import bke.iso.engine.render.DebugCircle
import bke.iso.engine.render.DebugData
import bke.iso.engine.render.DebugPoint
import bke.iso.game.Player
import bke.iso.game.Turret
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
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
            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)

            val debugData = getDebugData(entity)
            debugData.circles.add(DebugCircle(visionRadius, Color.GOLD))

            val playerEntity = entityService.getAll()
                .firstOrNull { it.has<Player>() }
                ?: return@withComponent

            val visionRange = Circle(entity.x, entity.y, visionRadius)
            if (visionRange.contains(playerEntity.x, playerEntity.y)) {
                getDebugData(playerEntity)
                    .points
                    .add(DebugPoint(2f, Color.YELLOW))
            }
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

//    private fun updateTurret(entity: Entity, turret: Turret, deltaTime: Float) {
//        if (turret.coolDownTime > 0f) {
//            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)
//        }
//
//        // TODO: targeting is dependent on player.
//        //  add support for targeting any enemy entity
//        val playerPos = findPlayerPos() ?: return
//        val pos = Vector2(entity.x, entity.y)
//        entity.add(DebugLine(pos, playerPos))
//        entity.add(DebugRectangle(getSegmentRectangle(pos, playerPos)))
//
//        entity.add(DebugCircle(Circle(pos, visionRadius), Color.GOLD))
//
//        val turretToPlayer = Segment(
//            Vector3(pos.x, pos.y, 0f),
//            Vector3(playerPos.x, playerPos.y, 0f)
//        )
//        collisionService.checkSegmentCollisions(turretToPlayer)
//            .firstOrNull { collision ->
//                val otherEntity = collision.data.entity
//                entity != otherEntity && entityIsInRange(entity, otherEntity)
//            }?.let { collision ->
//                val otherEntity = collision.data.entity
//
//                otherEntity.add(
//                    DebugPoints(
//                        collisionService.findIntersectionPoints(turretToPlayer, collision.data.box)
//                    )
//                )
//
//                if (otherEntity.has<Player>() && turret.coolDownTime == 0f) {
//                    engine.fireEvent(ShootEvent(entity, playerPos, BulletType.TURRET))
//                    turret.coolDownTime = coolDownTime
//                }
//            }
//    }

//    private fun entityIsInRange(turretEntity: Entity, entity: Entity) =
//        Vector2(turretEntity.x, turretEntity.y)
//            .dst(entity.x, entity.y) <= visionRadius
//
//
//    private fun getSegmentRectangle(start: Vector2, end: Vector2): Rectangle {
//        val min = Vector2(
//            min(start.x, end.x),
//            min(start.y, end.y)
//        )
//        val max = Vector2(
//            max(start.x, end.x),
//            max(start.y, end.y)
//        )
//        return Rectangle(
//            min.x,
//            min.y,
//            max.x - min.x,
//            max.y - min.y
//        )
//    }
}
