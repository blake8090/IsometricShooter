package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.Engine
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.game.event.BulletType
import bke.iso.game.Player
import bke.iso.game.event.ShootEvent
import bke.iso.game.Turret
import com.badlogic.gdx.math.Vector2
import kotlin.math.max

class TurretController(
    private val entityService: EntityService,
    private val engine: Engine
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

        val playerPos = getPlayerPos()
        if (turret.coolDownTime == 0f && playerPos != null) {
            turret.coolDownTime = 3f
            engine.fireEvent(ShootEvent(entity, playerPos, BulletType.TURRET))
        }
    }

    private fun getPlayerPos(): Vector2? {
        val player = entityService.getAll().firstOrNull { entity -> entity.has<Player>() }
        return if (player == null) {
            null
        } else {
            Vector2(player.x, player.y)
        }
    }
}
