package bke.iso.v2.game.event

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.v2.engine.entity.Entity
import bke.iso.v2.engine.event.Event
import bke.iso.v2.engine.event.EventHandler
import bke.iso.v2.game.Health
import bke.iso.v2.game.Player
import kotlin.math.max

data class DamageEvent(
    val sourceEntity: Entity,
    val targetEntity: Entity,
    val amount: Float
) : Event()

@Transient
class DamageHandler : EventHandler<DamageEvent> {
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
                event.targetEntity.delete()
            }
            // todo: fire death event!
        }
    }
}
