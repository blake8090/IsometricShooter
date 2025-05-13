package bke.iso.game.weapon.system

import bke.iso.engine.state.System
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import kotlin.math.max

class ExplosionSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.entities.each { entity: Entity, explosion: Explosion ->
            explosion.timer = max(0f, explosion.timer - deltaTime)

            if (explosion.timer == 0f) {
                world.delete(entity)
            } else {
                val sprite = checkNotNull(entity.get<Sprite>()) {
                    "Expected sprite for (Explosion) entity $entity"
                }
                sprite.alpha = (explosion.timer / explosion.timeSeconds)
            }
        }
    }
}
