package bke.iso.game.combat

import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.asset.config.Configs
import bke.iso.engine.core.Module
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.World
import bke.iso.game.entity.player.Player
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

    override val alwaysActive: Boolean = false

    fun applyDamage(entity: Entity, damage: Float) {
        val health = entity.get<Health>() ?: return
        health.value = max(health.value - damage, 0f)
        log.debug { "Actor $entity received damage: $damage Remaining health: ${health.value}" }

        if (entity.has<Player>()) {
            events.fire(PlayerHealthChangeEvent(health.value))
        }

        val config = configs.get<CombatConfig>("combat.cfg")
        entity.add(HitEffect(config.hitEffectDurationSeconds))

        if (health.value == 0f) {
            onDeath(entity)
        }
    }

    private fun onDeath(entity: Entity) {
        if (entity.has<Player>()) {
            return
        }
        world.delete(entity)
        log.debug { "Actor $entity has been destroyed" }
    }

    fun heal(entity: Entity) {
        val health = entity.get<Health>() ?: return

        if (entity.has<HealEffect>()) {
            return
        }

        val config = configs.get<CombatConfig>("combat.cfg")
        val amountPerSecond = health.maxValue * config.medkitHealthPercentage
        entity.add(HealEffect(amountPerSecond, config.medkitDurationSeconds))
    }

    data class PlayerHealthChangeEvent(val health: Float) : Event
}
