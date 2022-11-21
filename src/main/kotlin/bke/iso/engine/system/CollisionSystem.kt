package bke.iso.engine.system

import bke.iso.engine.entity.Component
import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import java.util.UUID

data class CollisionBounds(
    val width: Float,
    val length: Float,
    val offset: Vector2 = Vector2()
)

data class Collision(
    val bounds: CollisionBounds,
    val solid: Boolean = false
) : Component()

data class CollisionEvent(
    val id: UUID,
    val collisionArea: Rectangle,
    val solid: Boolean
)

data class CollisionEvents(
    val events: List<CollisionEvent>
) : Component()

/**
 * Contains collision info for the current frame.
 *
 * Used by the [PhysicsSystem] for movement, and the renderer for debug purposes.
 */
data class CollisionFrameData(
    val collisionArea: Rectangle,
    val projectedAreaX: Rectangle?,
    val projectedAreaY: Rectangle?,
    val solidCollisionX: CollisionEvent?,
    val solidCollisionY: CollisionEvent?
) : Component()

class CollisionSystem(private val entities: Entities) : System {
    override fun update(deltaTime: Float) {
        // TODO: use remove all?
        entities.withComponent(CollisionFrameData::class) { entity, _ ->
            entity.removeComponent(CollisionFrameData::class)
        }
        entities.withComponent(CollisionEvents::class) { entity, _ ->
            entity.removeComponent(CollisionEvents::class)
        }

        entities.withComponent(Collision::class) { entity, collision ->
            val collisionArea = getCollisionArea(entity.getPos(), collision.bounds)
            val collisions = findCollisions(entity, collision, collisionArea)

            val velocity = entity.getComponent<Velocity>()
            val dx = velocity?.dx ?: 0f
            val dy = velocity?.dy ?: 0f
            val projection = projectCollisionArea(collisionArea, dx, dy)

            val projectedAreaX = projection.first
            val xCollisions = projectedAreaX
                ?.let { area -> findCollisions(entity, collision, area) }
                ?: emptyList()
            val solidCollisionX = xCollisions.firstOrNull(CollisionEvent::solid)

            val projectedAreaY = projection.second
            val yCollisions = projectedAreaY
                ?.let { area -> findCollisions(entity, collision, area) }
                ?: emptyList()
            val solidCollisionY = yCollisions.firstOrNull(CollisionEvent::solid)

            val events = collisions + xCollisions + yCollisions
                .distinctBy { collisionEvent -> collisionEvent.id }

            if (events.isNotEmpty()) {
                entity.addComponent(CollisionEvents(events))
            }

            entity.addComponent(
                CollisionFrameData(
                    collisionArea,
                    projectedAreaX,
                    projectedAreaY,
                    solidCollisionX,
                    solidCollisionY
                )
            )
        }
    }

    private fun projectCollisionArea(collisionArea: Rectangle, dx: Float, dy: Float): Pair<Rectangle?, Rectangle?> {
        val projectedAreaX =
            if (dx != 0f) {
                createProjectionX(collisionArea, dx)
            } else {
                null
            }
        val projectedAreaY =
            if (dy != 0f) {
                createProjectionY(collisionArea, dy)
            } else {
                null
            }
        return projectedAreaX to projectedAreaY
    }

    private fun getCollisionArea(position: Vector2, bounds: CollisionBounds): Rectangle =
        Rectangle(
            position.x + bounds.offset.x,
            position.y + bounds.offset.y,
            bounds.width,
            bounds.length
        )

    private fun findCollisions(entity: Entity, collision: Collision, collisionArea: Rectangle): List<CollisionEvent> =
        entities.inArea(collisionArea)
            .mapNotNull { otherEntity -> toCollisionEvent(otherEntity) }
            .filter { event -> isValidEvent(entity, collision, event) }
            .filter { event -> event.collisionArea.overlaps(collisionArea) }

    private fun toCollisionEvent(entity: Entity): CollisionEvent? {
        val collision = entity.getComponent<Collision>() ?: return null
        return CollisionEvent(
            entity.id,
            getCollisionArea(entity.getPos(), collision.bounds),
            collision.solid
        )
    }

    private fun isValidEvent(entity: Entity, collision: Collision, collisionEvent: CollisionEvent): Boolean {
        if (entity.id == collisionEvent.id) {
            return false
        }

        if (collision.solid && collisionEvent.solid) {
            return false
        }

        return true
    }

    private fun createProjectionX(collisionArea: Rectangle, dx: Float): Rectangle {
        val projectedArea = Rectangle(collisionArea)
        if (dx > 0) {
            projectedArea.width += dx
        } else {
            projectedArea.x += dx
            projectedArea.width -= dx
        }
        return projectedArea
    }

    private fun createProjectionY(collisionArea: Rectangle, dy: Float): Rectangle {
        val projectedArea = Rectangle(collisionArea)
        if (dy > 0) {
            projectedArea.height += dy
        } else {
            projectedArea.y += dy
            projectedArea.height -= dy
        }
        return projectedArea
    }
}
