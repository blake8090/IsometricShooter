package bke.iso.old.game.entity

import bke.iso.old.engine.entity.Component
import bke.iso.old.engine.system.System
import bke.iso.old.engine.world.WorldService
import com.badlogic.gdx.math.Vector3
import java.util.UUID

data class Bullet(
    val shooterId: UUID,
    val startPos: Vector3
) : Component()

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
