package bke.iso.game.system

import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService
import bke.iso.game.Bullet
import com.badlogic.gdx.math.Vector3

class BulletSystem(private val worldService: WorldService) : System {
    override fun update(deltaTime: Float) {
        worldService.entities.withComponent(Bullet::class) { entity, bullet ->
            val pos = Vector3(entity.x, entity.y, 0f)
            val distance = bullet.startPos.dst(pos)
            if (distance > 50f) {
                worldService.delete(entity)
            }
        }
    }
}
