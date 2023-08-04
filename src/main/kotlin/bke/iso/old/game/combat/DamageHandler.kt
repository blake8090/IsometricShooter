package bke.iso.old.game.combat

import bke.iso.old.engine.log
import bke.iso.old.engine.entity.Entity
import bke.iso.old.engine.event.Event
import bke.iso.old.engine.event.EventHandler
import bke.iso.old.engine.world.WorldService
import bke.iso.old.game.entity.Player
import kotlin.math.max

data class DamageEvent(
    val sourceEntity: Entity,
    val targetEntity: Entity,
    val amount: Float
) : Event()

class DamageHandler(private val worldService: WorldService) : EventHandler<DamageEvent> {
    override val type = DamageEvent::class

    override fun handle(event: DamageEvent) {
        if (event.sourceEntity == event.targetEntity) {
            return
        }

        val health = event.targetEntity.get<Health>() ?: return
        val remaining = max(0f, health.value - event.amount)
        log.trace("target received ${event.amount} damage. Remaining health $remaining")
        health.value = remaining
        if (health.value == 0f) {
            log.trace("dead")
            if (!event.targetEntity.has<Player>()) {
                worldService.delete(event.targetEntity)
            }
            // todo: fire death event!
        }
    }
}
