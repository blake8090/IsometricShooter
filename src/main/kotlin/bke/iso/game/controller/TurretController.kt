package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.CollisionService
import bke.iso.engine.render.DebugLine
import bke.iso.engine.render.DebugRectangle
import bke.iso.game.Player
import bke.iso.game.Turret
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.max
import kotlin.math.min

class TurretController(
    private val entityService: EntityService,
    private val collisionService: CollisionService
) : Controller {
    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Turret::class) { entity, turret ->
            updateTurret(entity, turret, deltaTime)
        }
    }

    private fun updateTurret(entity: Entity, turret: Turret, deltaTime: Float) {
        if (turret.coolDownTime > 0f) {
            turret.coolDownTime = max(0f, turret.coolDownTime - deltaTime)
        }

        val playerPos = findPlayerPos() ?: return
        val pos = Vector2(entity.x, entity.y)
        entity.add(DebugLine(pos, playerPos))
        entity.add(DebugRectangle(getSegmentRectangle(pos, playerPos)))
        // TODO: shoot when player in LOS
    }

    private fun findPlayerPos(): Vector2?  =
        entityService.getAll()
            .filter { entity -> entity.has<Player>() }
            .map { entity -> Vector2(entity.x, entity.y) }
            .firstOrNull()

    private fun getSegmentRectangle(start: Vector2, end: Vector2): Rectangle {
        val min = Vector2(
            min(start.x, end.x),
            min(start.y, end.y)
        )
        val max = Vector2(
            max(start.x, end.x),
            max(start.y, end.y)
        )
        return Rectangle(
            min.x,
            min.y,
            max.x - min.x,
            max.y - min.y
        )
    }
}
