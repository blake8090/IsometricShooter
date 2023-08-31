package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.physics.PhysicsMode
import bke.iso.engine.physics.PhysicsBody
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.Collision
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.debug.DebugSettings
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.Description
import bke.iso.engine.world.GameObject
import bke.iso.engine.world.World
import bke.iso.game.combat.Combat
import com.badlogic.gdx.math.Vector3

enum class BulletType(
    val speed: Float,
    val damage: Float,
    val zOffset: Float
) {
    PLAYER(30f, 1f, 0.7f),
    TURRET(25f, 1f, 0.2f)
}

data class Bullet(
    val shooterId: String,
    val startPos: Vector3,
    val type: BulletType
) : Component()

private const val MAX_BULLET_DISTANCE = 50f

class BulletSystem(
    private val world: World,
    private val combat: Combat,
    private val collisions: Collisions
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
            if (obj.has<Bullet>() || obj.id == bullet.shooterId) {
                return
            }
            combat.applyDamage(obj, bullet.type.damage)
        }
        world.delete(actor)
    }

    private fun getFirstCollidingObject(actor: Actor) =
        collisions.getCollisions(actor)
            .minByOrNull(Collision::distance)
            ?.obj

}

fun World.createBullet(shooter: Actor, direction: Vector3, bulletType: BulletType): Actor {
    val pos = shooter.pos
    return newActor(
        pos.x,
        pos.y,
        pos.z + bulletType.zOffset,
        Bullet(shooter.id, pos, bulletType),
        Sprite("bullet", 8f, 8f),
        PhysicsBody(mode = PhysicsMode.GHOST, velocity = Vector3(direction).scl(bulletType.speed)),
        Collider(
            Vector3(0.125f, 0.125f, 0.125f),
            Vector3(0f, -0.125f, 0f)
        ),
        DebugSettings().apply {
            zAxis = false
        },
        Description("bullet")
    )
}
