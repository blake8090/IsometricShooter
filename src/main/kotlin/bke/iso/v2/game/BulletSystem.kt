package bke.iso.v2.game

import bke.iso.game.entity.Bullet
import bke.iso.v2.engine.System
import bke.iso.v2.engine.physics.FrameCollisions
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.engine.world.World

class BulletSystem(private val world: World) : System {

    override fun update(deltaTime: Float) {
        world.actorsWith<Bullet> { actor, bullet ->
            val firstCollision = actor.components[FrameCollisions::class]
                ?.collisions
                ?.firstOrNull()
                ?: return@actorsWith

            val other = firstCollision.obj
            val isBullet = other is Actor && Bullet::class in other.components
            if (other == actor || other.id == bullet.shooterId || isBullet) {
                return@actorsWith
            }
            world.delete(actor)
        }
    }
}
