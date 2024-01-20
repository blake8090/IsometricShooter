package bke.iso.game.weapon

import bke.iso.engine.System
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.Collisions
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.World
import bke.iso.game.combat.CombatModule

private const val MAX_BULLET_DISTANCE = 50f

class BulletSystem(
    private val world: World,
    private val combatModule: CombatModule,
    private val collisions: Collisions
) : System {

    override fun update(deltaTime: Float) {
        world.actors.each<Bullet> { actor, bullet ->
            update(actor, bullet)
        }
    }

    private fun update(actor: Actor, bullet: Bullet) {
        val distance = bullet.start.dst(actor.pos)
        if (distance > MAX_BULLET_DISTANCE) {
            world.delete(actor)
            return
        }
        getFirstCollidingObject(actor)?.let { obj ->
            handleCollision(actor, bullet, obj)
        }
    }

    private fun handleCollision(actor: Actor, bullet: Bullet, obj: GameObject) {
        if (obj is Actor) {
            if (obj.has<Bullet>() || obj.id == bullet.shooterId) {
                return
            }
            combatModule.applyDamage(obj, bullet.damage)
        }
        world.delete(actor)
    }

    private fun getFirstCollidingObject(actor: Actor) =
        collisions.getCollisions(actor)
            .minByOrNull(Collision::distance)
            ?.obj

}
