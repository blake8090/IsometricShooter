package bke.iso.game.combat

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import bke.iso.game.actor.BulletType
import bke.iso.game.player.Player
import bke.iso.game.actor.createBullet
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.max

data class PlayerDamageEvent(val health: Float) : Event

class Combat(private val world: World, private val events: Game.Events) {

    private val log = KotlinLogging.logger {}

    fun shoot(shooter: Actor, target: Vector3, bulletType: BulletType) {
        val pos = shooter.pos
        val direction = Vector3(target).sub(pos).nor()
        world.createBullet(shooter, direction, bulletType)
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
}
