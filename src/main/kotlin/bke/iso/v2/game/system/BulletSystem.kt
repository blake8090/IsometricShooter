package bke.iso.v2.game.system

import bke.iso.service.Transient
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.system.System
import bke.iso.v2.game.Bullet
import com.badlogic.gdx.math.Vector2

@Transient
class BulletSystem(private val entityService: EntityService) : System {
    override fun update(deltaTime: Float) {
        entityService.search.withComponent(Bullet::class) { entity, bullet ->
            val pos = Vector2(entity.x, entity.y)
            val distance = bullet.startPos.dst(pos)
            if (distance > 50f) {
                entity.delete()
            }
        }
    }
}
