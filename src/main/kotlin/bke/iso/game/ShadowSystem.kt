package bke.iso.game

import bke.iso.engine.System
import bke.iso.engine.math.Box
import bke.iso.engine.physics.collision.Collider
import bke.iso.engine.physics.collision.Collisions
import bke.iso.engine.physics.collision.getCollisionData
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.Description
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import java.util.UUID

data class Shadow(val parent: UUID) : Component()

private const val Z_OFFSET = 0.0001f
private const val SHADOW_ALPHA = 0.5f

class ShadowSystem(
    private val world: World,
    private val collisions: Collisions
) : System {

    override fun update(deltaTime: Float) {
        world.actorsWith { actor: Actor, shadow: Shadow ->
            val parent = world.getActor(shadow.parent)
            val z = findNearestZ(parent)
            actor.moveTo(parent.x, parent.y, z)
        }
    }

    private fun findNearestZ(actor: Actor): Float {
        val size = actor.getCollisionData()
            ?.box
            ?.size
            ?: Vector3(1f, 1f, 0f)
        val zLimit = 5f

        val min = Vector3(
            actor.x - (size.x / 2f),
            actor.y - (size.y / 2f),
            actor.z - zLimit
        )
        val max = Vector3(
            actor.x + (size.x / 2f),
            actor.y + (size.y / 2f),
            actor.z + Z_OFFSET
        )
        val area = Box.from(min, max)
        val tallestCollision = collisions
            .checkCollisions(area)
            .filter { collision ->
                if (actor == collision.obj) {
                    false
                } else if (collision.obj is Actor && collision.obj.has<Shadow>()) {
                    false
                } else {
                    true
                }
            }
            .minByOrNull { actor.z - it.box.max.z }
        return if (tallestCollision == null) {
            Z_OFFSET
        } else {
            tallestCollision.box.max.z + Z_OFFSET
        }
    }
}

fun World.createShadow(actor: Actor): Actor =
    newActor(
        actor.x,
        actor.y,
        actor.z,
        // TODO: sprite offsets should be negative for consistency!
        Sprite("shadow", 16f, 16f, SHADOW_ALPHA),
        Shadow(actor.id),
        Collider(
            false,
            Vector3(0.25f, 0.25f, Z_OFFSET),
            Vector3(-0.125f, -0.125f, 0f)
        ),
        Description("shadow for $actor")
    )
