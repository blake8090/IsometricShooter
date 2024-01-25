package bke.iso.game.weapon.system

import bke.iso.engine.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.nextFloat
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.game.combat.CombatModule
import bke.iso.game.weapon.Bullet
import bke.iso.game.weapon.Explosion
import com.badlogic.gdx.math.Vector3

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

        val firstCollision = collisions
            .getCollisions(actor)
            .minByOrNull(Collision::distance)

        if (firstCollision != null) {
            handleCollision(actor, bullet, firstCollision)
        }
    }

    private fun handleCollision(actor: Actor, bullet: Bullet, collision: Collision) {
        val obj = collision.obj
        if (obj is Actor && canShoot(obj, bullet.shooterId)) {
            combatModule.applyDamage(obj, bullet.damage)
        }
        createExplosion(actor.pos, collision)
        world.delete(actor)
    }

    private fun canShoot(target: Actor, shooterId: String): Boolean {
        if (target.id == shooterId) {
            return false
        }

        return !target.has<Bullet>() && !target.has<Explosion>()
    }

    private fun createExplosion(pos: Vector3, collision: Collision) {
        val explosion = world.actors.create(
            pos,
            Explosion(0.25f),
            Collider(
                size = Vector3(0.25f, 0.25f, 0.25f),
                offset = Vector3(-0.125f, -0.125f, -0.125f)
            ),
            Sprite(
                "bullet.png",
                scale = nextFloat(0.75f, 1f),
                offsetX = 8f,
                offsetY = 8f
            ),
            PhysicsBody(mode = PhysicsMode.GHOST)
        )
        clampPosToCollisionSide(explosion, collision)
    }

    // TODO: make this a util
    private fun clampPosToCollisionSide(actor: Actor, collision: Collision) {
        val box = checkNotNull(actor.getCollisionBox()) {
            "Expected collision box for $actor"
        }
        val otherBox = checkNotNull(collision.obj.getCollisionBox()) {
            "Expected collision box for $collision.obj"
        }

        var x = actor.x
        var y = actor.y
        var z = actor.z
        when (collision.side) {
            CollisionSide.LEFT -> {
                x = otherBox.min.x - (box.size.x / 2f)
            }

            CollisionSide.RIGHT -> {
                x = otherBox.max.x + (box.size.x / 2f)
            }

            CollisionSide.FRONT -> {
                y = otherBox.min.y - (box.size.y / 2f)
            }

            CollisionSide.BACK -> {
                y = otherBox.max.y + (box.size.y / 2f)
            }

            CollisionSide.TOP -> {
                z = otherBox.max.z // actor origins are at the bottom of the collision box, not the center
            }

            CollisionSide.BOTTOM -> {
                z = otherBox.min.z - box.size.z // actor origins are at the bottom of the collision box, not the center
            }

            CollisionSide.CORNER -> {
                // log.warn { "Could not resolve corner collision" }
            }
        }
        actor.moveTo(x, y, z)
    }
}