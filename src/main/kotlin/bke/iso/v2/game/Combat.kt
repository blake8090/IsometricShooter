package bke.iso.v2.game

import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.Component
import bke.iso.v2.engine.world.World
import bke.iso.v2.game.actor.BulletType
import bke.iso.v2.game.actor.Player
import bke.iso.v2.game.actor.createBullet
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

class Combat(private val world: World) {

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
