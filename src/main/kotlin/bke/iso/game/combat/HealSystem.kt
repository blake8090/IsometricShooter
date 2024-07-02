package bke.iso.game.combat

import bke.iso.engine.Events
import bke.iso.engine.state.System
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.actor.player.Player
import kotlin.math.min

class HealSystem(
    private val world: World,
    private val events: Events
) : System {

    override fun update(deltaTime: Float) {
        world.actors.each<HealEffect> { actor, healEffect ->
            update(actor, healEffect, deltaTime)
        }
    }

    private fun update(actor: Actor, healEffect: HealEffect, deltaTime: Float) {
        val health = actor.get<Health>()
        if (health == null) {
            actor.remove<HealEffect>()
            return
        }

        val amount = healEffect.amountPerSecond * deltaTime
        health.value = min(health.value + amount, health.maxValue)
        // TODO: should healing effect stop early when at full health?

        if (actor.has<Player>()) {
            events.fire(CombatModule.PlayerHealthChangeEvent(health.value))
        }

        healEffect.elapsedTime += deltaTime
        if (healEffect.elapsedTime >= healEffect.durationSeconds) {
            actor.remove<HealEffect>()
        }
    }
}
