package bke.iso.game.weapon.system

import bke.iso.engine.state.System
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.weapon.Explosion
import kotlin.math.max

class ExplosionSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, explosion: Explosion ->
            explosion.timer = max(0f, explosion.timer - deltaTime)

            if (explosion.timer == 0f) {
                world.delete(actor)
            } else {
                val sprite = checkNotNull(actor.get<Sprite>()) {
                    "Expected sprite for (Explosion) actor $actor"
                }
                sprite.alpha = (explosion.timer / explosion.timeSeconds)
            }
        }
    }
}
