package bke.iso.v2.game

import bke.iso.engine.log
import bke.iso.game.combat.Health
import bke.iso.game.entity.Player
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.World
import com.badlogic.gdx.math.Vector3
import kotlin.math.max

class Combat(private val world: World) {

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
        world.delete(actor)
        log.trace("Actor ${actor.id} has been destroyed")
    }
}
