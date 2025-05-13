package bke.iso.game.combat.system

import bke.iso.engine.core.Events
import bke.iso.engine.state.System
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.game.entity.player.Player
import bke.iso.game.combat.CombatModule
import kotlin.math.min

class HealSystem(
    private val world: World,
    private val events: Events
) : System {

    override fun update(deltaTime: Float) {
        world.entities.each<HealEffect> { entity, healEffect ->
            update(entity, healEffect, deltaTime)
        }
    }

    private fun update(entity: Entity, healEffect: HealEffect, deltaTime: Float) {
        val health = entity.get<Health>()
        if (health == null) {
            entity.remove<HealEffect>()
            return
        }

        val amount = healEffect.amountPerSecond * deltaTime
        health.value = min(health.value + amount, health.maxValue)
        // TODO: should healing effect stop early when at full health?

        if (entity.has<Player>()) {
            events.fire(CombatModule.PlayerHealthChangeEvent(health.value))
        }

        healEffect.elapsedTime += deltaTime
        if (healEffect.elapsedTime >= healEffect.durationSeconds) {
            entity.remove<HealEffect>()
        }
    }
}
