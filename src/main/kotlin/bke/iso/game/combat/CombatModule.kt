package bke.iso.game.combat

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import bke.iso.game.player.Player
import mu.KotlinLogging
import kotlin.math.max

class CombatModule(
    private val world: World,
    private val events: Game.Events
) : Module {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
    }

    override fun handleEvent(event: Event) {
    }

    fun applyDamage(actor: Actor, damage: Float) {
        val health = actor.get<Health>() ?: return
        health.value = max(health.value - damage, 0f)
        log.debug { "Actor $actor received damage: $damage Remaining health: ${health.value}" }

        if (actor.has<Player>()) {
            events.fire(PlayerDamageEvent(health.value))
        }

        if (health.value == 0f) {
            onDeath(actor)
        }
    }

    private fun onDeath(actor: Actor) {
        if (actor.has<Player>()) {
            return
        }
        world.actors.delete(actor)
        log.debug { "Actor $actor has been destroyed" }
    }

    data class PlayerDamageEvent(val health: Float) : Event
}
