package bke.iso.game

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.World
import bke.iso.game.actor.BulletType
import bke.iso.game.actor.Player
import bke.iso.game.actor.createBullet
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import kotlin.math.max

data class Health(
    val maxValue: Float,
    var value: Float = maxValue
) : Component()

data class HealthBar(
    val offsetX: Float,
    val offsetY: Float
) : Component()

data class OnDamagePlayerEvent(
    val health: Float
) : Event()

class Combat(
    private val world: World,
    private val events: Game.Events
) {

    private val log = KotlinLogging.logger {}

    fun shoot(shooter: Actor, target: Vector3, bulletType: BulletType) {
        val pos = shooter.pos
        val direction = Vector3(target).sub(pos).nor()
        world.createBullet(shooter, direction, bulletType)
    }

    fun onDamage(actor: Actor, damage: Float) {
        val health = actor.components[Health::class] ?: return
        health.value = max(health.value - damage, 0f)
        log.trace("Actor received damage: $damage Remaining health: ${health.value}")

        if (actor.has<Player>()) {
            events.fire(OnDamagePlayerEvent(health.value))
        }

        if (health.value == 0f) {
            onDeath(actor)
        }
    }

    private fun onDeath(actor: Actor) {
        if (actor.has<Player>()) {
            return
        }
        world.deleteActor(actor)
        log.trace("Actor ${actor.id} has been destroyed")
    }
}
