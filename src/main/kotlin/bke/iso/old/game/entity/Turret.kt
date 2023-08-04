package bke.iso.old.game.entity

import bke.iso.old.engine.entity.Component
import bke.iso.old.engine.entity.Entity
import bke.iso.old.engine.event.EventService
import bke.iso.old.engine.physics.CollisionService
import bke.iso.old.engine.physics.ObjectSegmentCollision
import bke.iso.old.engine.render.debug.DebugRenderService
import bke.iso.old.engine.system.System
import bke.iso.old.engine.world.WorldService
import bke.iso.old.game.combat.BulletType
import bke.iso.old.game.combat.ShootEvent
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.max

data class Turret(var coolDownTime: Float = 0f) : Component()

private const val VISION_RADIUS = 12f
private const val COOLDOWN_SECONDS = 0.5f

class TurretSystem(
    private val worldService: WorldService,
    private val collisionService: CollisionService,
    private val eventService: EventService,
    private val debugRenderService: DebugRenderService
) : System {

    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Turret::class) { entity, turret ->
            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)
            debugRenderService.addSphere(entity.pos, VISION_RADIUS, Color.GOLD)
            attackPlayer(entity, turret)
        }
    }

    private fun attackPlayer(entity: Entity, turret: Turret) {
        val start = entity.pos
        val playerEntity = worldService.entities.firstHaving<Player>() ?: return
        val target = playerEntity.pos

        val firstCollision = collisionService
            .checkCollisions(Segment(start, target))
            .asSequence()
            .sortedBy(ObjectSegmentCollision::distanceStart)
            .filter { collision -> collision.obj is Entity }
            .filter { collision -> start.dst(collision.data.box.center) <= VISION_RADIUS }
            .filter { collision -> collision.obj != entity }
            .firstOrNull()
            ?: return

        val firstPoint = firstCollision
            .points
            .minByOrNull { point -> start.dst(point) }!!
        debugRenderService.addPoint(firstPoint, 3f, Color.RED)

        if (firstCollision.obj != playerEntity) {
            debugRenderService.addLine(start, firstPoint, 1f, Color.RED)
            return
        }

        debugRenderService.addLine(start, target, 1f, Color.RED)
        if (turret.coolDownTime == 0f) {
            eventService.fire(ShootEvent(entity, target, BulletType.TURRET))
            turret.coolDownTime = COOLDOWN_SECONDS
        }
    }
}
