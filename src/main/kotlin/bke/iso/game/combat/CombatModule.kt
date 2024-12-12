package bke.iso.game.combat

import bke.iso.engine.Event
import bke.iso.engine.Events
import bke.iso.engine.asset.config.Configs
import bke.iso.engine.core.Module
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import bke.iso.game.actor.player.Player
import bke.iso.game.combat.system.HealEffect
import bke.iso.game.combat.system.Health
import bke.iso.game.combat.system.HitEffect
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.max

//private const val MEDKIT_HEALTH_PERCENTAGE = 0.05f
//private const val MEDKIT_DURATION_SECONDS = 10f
//private const val HIT_EFFECT_DURATION_SECONDS = 0.05f

class CombatModule(
    private val world: World,
    private val events: Events,
    private val configs: Configs
) : Module {

    private val log = KotlinLogging.logger {}

    fun applyDamage(actor: Actor, damage: Float) {
        val health = actor.get<Health>() ?: return
        health.value = max(health.value - damage, 0f)
        log.debug { "Actor $actor received damage: $damage Remaining health: ${health.value}" }

        if (actor.has<Player>()) {
            events.fire(PlayerHealthChangeEvent(health.value))
        }

        val config = configs.get<CombatConfig>("combat.cfg")
        actor.add(HitEffect(config.hitEffectDurationSeconds))

        if (health.value == 0f) {
            onDeath(actor)
        }
    }

    private fun onDeath(actor: Actor) {
        if (actor.has<Player>()) {
            return
        }
        world.delete(actor)
        log.debug { "Actor $actor has been destroyed" }
    }

    fun heal(actor: Actor) {
        val health = actor.get<Health>() ?: return

        if (actor.has<HealEffect>()) {
            return
        }

        val config = configs.get<CombatConfig>("combat.cfg")
        val amountPerSecond = health.maxValue * config.medkitHealthPercentage
        actor.add(HealEffect(amountPerSecond, config.medkitDurationSeconds))
    }

    data class PlayerHealthChangeEvent(val health: Float) : Event
}
