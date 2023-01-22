package bke.iso.game.system

import bke.iso.service.Transient
import bke.iso.engine.entity.EntityService
import bke.iso.engine.system.System
import bke.iso.game.Bullet
import com.badlogic.gdx.math.Vector3

@Transient
class BulletSystem(private val entityService: EntityService) : System {
    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Bullet::class) { entity, bullet ->
            val pos = Vector3(entity.x, entity.y, 0f)
            val distance = bullet.startPos.dst(pos)
            if (distance > 50f) {
                entity.delete()
            }
        }
    }
}
