package bke.iso.game.controller

import bke.iso.engine.Controller
import bke.iso.engine.entity.EntityService
import bke.iso.game.Bullet
import com.badlogic.gdx.math.Vector2

class BulletController(private val entityService: EntityService) : Controller {
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
