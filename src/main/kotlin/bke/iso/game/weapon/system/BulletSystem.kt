package bke.iso.game.weapon.system

import bke.iso.engine.state.System
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.CollisionSide
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.math.nextFloat
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.Tile
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Description
import bke.iso.game.combat.CombatModule
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

private const val MAX_BULLET_DISTANCE = 50f

class BulletSystem(
    private val world: World,
    private val combatModule: CombatModule,
    private val collisions: Collisions
) : System {

    private val log = KotlinLogging.logger {}

    override fun update(deltaTime: Float) {
        world.actors.each<Bullet> { bulletActor, bullet ->
            update(bulletActor, bullet)
        }
    }

    private fun update(bulletActor: Actor, bullet: Bullet) {
        val distance = bullet.start.dst(bulletActor.pos)
        if (distance > MAX_BULLET_DISTANCE) {
            world.delete(bulletActor)
            return
        }

        val firstCollision = collisions
            .getCollisions(bulletActor)
            .sortedBy(Collision::distance)
            .firstOrNull { collision -> canCollide(bullet, collision.obj) }

        if (firstCollision != null) {
            handleCollision(bulletActor, bullet, firstCollision)
        }
    }

    private fun canCollide(bullet: Bullet, obj: GameObject): Boolean =
        when (obj) {
            is Tile -> {
                true
            }

            is Actor -> {
                obj.id != bullet.shooterId
                        && !obj.has<Bullet>()
                        && !obj.has<Explosion>()
            }

            else -> {
                false
            }
        }

    private fun handleCollision(bulletActor: Actor, bullet: Bullet, collision: Collision) {
        val target = collision.obj
        if (target is Actor) {
            val damage = calculateDamage(bulletActor, bullet)
            combatModule.applyDamage(target, damage)
        }

        createExplosion(bulletActor.pos, collision)
        world.delete(bulletActor)
    }

    private fun calculateDamage(bulletActor: Actor, bullet: Bullet): Float {
        val distance = bulletActor.pos.dst(bullet.start)
        if (distance <= bullet.range) {
            return bullet.damage
        }

        val ratio = bullet.range / distance
        val damage = bullet.damage * ratio
        log.debug { "Bullet range: ${bullet.range} dist: $distance ratio: $ratio damage: $damage" }

        return damage
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
            PhysicsBody(mode = PhysicsMode.GHOST),
            Description("explosion")
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
