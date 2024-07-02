package bke.iso.game.combat

import bke.iso.engine.state.System
import bke.iso.engine.render.SpriteFillColor
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor

class HitEffectSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actors.each { actor: Actor, hitEffect: HitEffect ->
            hitEffect.elapsedTime += deltaTime

            if (hitEffect.elapsedTime > hitEffect.durationSeconds) {
                actor.remove<HitEffect>()
                actor.remove<SpriteFillColor>()
            } else {
                actor.add(SpriteFillColor(1f, 1f, 1f))
            }
        }
    }
}
