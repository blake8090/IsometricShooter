package bke.iso.game

import bke.iso.engine.System
import bke.iso.engine.math.Box
import bke.iso.engine.physics.collision.Collider
import bke.iso.engine.physics.collision.Collision
import bke.iso.engine.physics.collision.Collisions
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.Component
import bke.iso.engine.world.Description
import bke.iso.engine.world.World
import com.badlogic.gdx.math.Vector3
import java.util.UUID

data class Shadow(val parent: UUID) : Component()

private const val Z_OFFSET = 0.0001f
private const val MAX_RANGE = 5f
private const val SPRITE_ALPHA = 0.5f

class ShadowSystem(
    private val world: World,
    private val collisions: Collisions
) : System {

    override fun update(deltaTime: Float) {
        world.actorsWith { actor: Actor, shadow: Shadow ->
            val sprite = actor.get<Sprite>() ?: return@actorsWith
            val parent = world.getActor(shadow.parent)

            val box = findTallestBoxBeneath(parent)
            val z = box?.max?.z ?: 0f
            val distance = parent.z - z
            // ratio between the distance from the parent to the shadow and the max z range
            val ratio = 1 - (distance / MAX_RANGE)
            sprite.alpha = ratio * SPRITE_ALPHA

            actor.moveTo(parent.x, parent.y, z + Z_OFFSET)
        }
    }

    private fun findTallestBoxBeneath(parent: Actor): Box? {
        val sizeX = 0.25f
        val sizeY = 0.25f
        val min = Vector3(
            parent.x - (sizeX / 2f),
            parent.y - (sizeY / 2f),
            parent.z - MAX_RANGE
        )
        val max = Vector3(
            parent.x + (sizeX / 2f),
            parent.y + (sizeY / 2f),
            parent.z + Z_OFFSET
        )
        val area = Box.from(min, max)
        return collisions
            .checkCollisions(area)
            .filter { collision -> filterCollision(parent, collision) }
            // instead of using the collision distance, which is the distance between two box's centers,
            // we need the distance between the top of the tallest box and the parent's z position.
            .minByOrNull { collision -> parent.z - collision.box.max.z }
            ?.box
    }

    private fun filterCollision(parent: Actor, collision: Collision) =
        if (parent == collision.obj) {
            false
        } else if (collision.obj is Actor && collision.obj.has<Shadow>()) {
            false
        } else {
            true
        }
}

fun World.createShadow(actor: Actor): Actor =
    newActor(
        actor.x,
        actor.y,
        actor.z,
        // TODO: sprite offsets should be negative for consistency!
        Sprite("shadow", 16f, 16f, SPRITE_ALPHA),
        Shadow(actor.id),
        Collider(
            false,
            Vector3(0.25f, 0.25f, Z_OFFSET),
            Vector3(-0.125f, -0.125f, 0f)
        ),
        Description("shadow for $actor")
    )