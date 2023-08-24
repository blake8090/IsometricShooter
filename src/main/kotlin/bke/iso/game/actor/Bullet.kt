package bke.iso.game.actor

import bke.iso.engine.System
import bke.iso.engine.physics.Collision
import bke.iso.engine.physics.FrameCollisions
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
) : System() {

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

        val other = getFirstCollidingObject(actor) ?: return
        if (other is Actor) {
            if (other.has<Bullet>()) {
                return
            }
            combat.onDamage(other, bullet.type.damage)
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
