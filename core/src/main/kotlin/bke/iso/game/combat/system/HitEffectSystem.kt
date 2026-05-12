package bke.iso.game.combat.system

import bke.iso.engine.state.System
import bke.iso.engine.render.SpriteFillColor
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity

class HitEffectSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.entities.each { entity: Entity, hitEffect: HitEffect ->
            hitEffect.elapsedTime += deltaTime

            if (hitEffect.elapsedTime > hitEffect.durationSeconds) {
                entity.remove<HitEffect>()
                entity.remove<SpriteFillColor>()
            } else {
                entity.add(SpriteFillColor(1f, 1f, 1f))
            }
        }
    }
}
