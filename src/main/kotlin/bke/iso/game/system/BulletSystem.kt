package bke.iso.game.system

import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService
import bke.iso.game.Bullet

class BulletSystem(private val worldService: WorldService) : System {
    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Bullet::class) { entity, bullet ->
            val distance = bullet.startPos.dst(entity.pos)
            if (distance > 50f) {
                worldService.delete(entity)
            }
        }
    }
}
