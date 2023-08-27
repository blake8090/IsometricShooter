package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.physics.collision.Collision
import bke.iso.engine.physics.collision.FrameCollisions
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.World
import bke.iso.game.Combat
import com.badlogic.gdx.math.Vector3
import java.util.UUID

enum class BulletType(
    val speed: Float,
    val damage: Float,
    val zOffset: Float
) {
    PLAYER(30f, 1f, 0.7f),
    TURRET(25f, 1f, 0.2f)
}

data class Bullet(
    val shooterId: UUID,
    val startPos: Vector3,
    val type: BulletType
) : Component()

private const val MAX_BULLET_DISTANCE = 50f

class BulletSystem(
    private val world: World,
    private val combat: Combat
) : System {

    override fun update(deltaTime: Float) {
        world.actorsWith<Bullet> { actor, bullet ->
            update(actor, bullet)
        }
    }

    private fun update(actor: Actor, bullet: Bullet) {
        val distance = bullet.startPos.dst(actor.pos)
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
            combat.onDamage(obj, bullet.type.damage)
            if (obj.has<Bullet>() || obj.id == bullet.shooterId) {
                return
            }
        }
        world.delete(actor)
    }

    private fun getFirstCollidingObject(actor: Actor): GameObject? {
        val collisions = actor.get<FrameCollisions>()
            ?.collisions
            ?: return null
        return collisions.minByOrNull(Collision::distance)?.obj
    }
}
