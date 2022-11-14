package bke.iso.engine.physics

import bke.iso.app.service.Service
import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

fun getCollisionArea(position: Vector2, bounds: CollisionBounds): Rectangle =
    Rectangle(
        position.x + bounds.offset.x,
        position.y + bounds.offset.y,
        bounds.width,
        bounds.length
    )

@Service
class Physics(private val entities: Entities) {
    fun update(deltaTime: Float) {
        entities.components.removeAll(CollisionProjection::class)
        entities.components.removeAll(CollisionEvent::class)

        entities.withComponent(Velocity::class) { entity, velocity ->
            val dx = velocity.dx * deltaTime
            val dy = velocity.dy * deltaTime
            moveEntity(entity, dx, dy)
            entity.removeComponent(Velocity::class)
        }
    }

    private fun moveEntity(entity: Entity, dx: Float, dy: Float) {
        if (dx == 0f && dy == 0f) {
            return
        }

        val pos = entity.getPos()
        val bounds = entity.getComponent<Collision>()?.bounds
        if (bounds == null) {
            entity.setX(pos.x + dx)
            entity.setY(pos.y + dy)
            return
        }

        val collisionArea = getCollisionArea(pos, bounds)
        val yProjection = createProjectionY(collisionArea, dy)
        val xProjection = createProjectionX(collisionArea, dx)
        // the debug renderer will use this component to draw the projected collision areas
        entity.addComponent(CollisionProjection(xProjection, yProjection))

        entity.setY(calculateY(entity, dy, bounds, yProjection))
        entity.setX(calculateX(entity, dx, bounds, xProjection))
    }

    private fun calculateY(
        entity: Entity,
        dy: Float,
        bounds: CollisionBounds,
        projectedCollisionArea: Rectangle
    ): Float {
        val pos = entity.getPos()
        val collisions = findCollisions(entity, projectedCollisionArea)
        recordCollisionEvent(entity, collisions)
        val solidCollision = collisions.firstOrNull(CollisionInfo::solid)
            ?: return pos.y + dy

        val collisionArea = getCollisionArea(pos, bounds)
        val otherCollisionArea = solidCollision.collisionArea
        return if (otherCollisionArea.y > collisionArea.y) {
            (otherCollisionArea.y + bounds.offset.y)
        } else {
            (otherCollisionArea.y + otherCollisionArea.height - bounds.offset.y)
        }
    }

    private fun calculateX(
        entity: Entity,
        dx: Float,
        bounds: CollisionBounds,
        projectedCollisionArea: Rectangle
    ): Float {
        val pos = entity.getPos()
        val collisions = findCollisions(entity, projectedCollisionArea)
        recordCollisionEvent(entity, collisions)
        val solidCollision = collisions.firstOrNull(CollisionInfo::solid)
            ?: return pos.x + dx

        val collisionArea = getCollisionArea(pos, bounds)
        val otherCollisionArea = solidCollision.collisionArea
        return if (otherCollisionArea.x > collisionArea.x) {
            (otherCollisionArea.x + bounds.offset.y)
        } else {
            (otherCollisionArea.x + otherCollisionArea.width - bounds.offset.x)
        }
    }

    private fun findCollisions(entity: Entity, collisionArea: Rectangle): List<CollisionInfo> =
        entities.inArea(collisionArea)
            .mapNotNull(this::getCollisionInfo)
            .filter { collisionData -> entity.id != collisionData.entity.id }
            .filter { collisionData -> collisionArea.overlaps(collisionData.collisionArea) }

    private fun getCollisionInfo(entity: Entity): CollisionInfo? {
        val collision = entity.getComponent<Collision>() ?: return null
        return CollisionInfo(
            entity,
            getCollisionArea(entity.getPos(), collision.bounds),
            collision.solid
        )
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

    private fun recordCollisionEvent(entity: Entity, collisions: List<CollisionInfo>) {
        val ids = collisions.map { collisionInfo -> collisionInfo.entity.id }
        if (ids.isEmpty()) {
            return
        }

        val collisionEvent = entity.getComponent<CollisionEvent>()
        if (collisionEvent == null) {
            entity.addComponent(CollisionEvent(ids.toMutableSet()))
            return
        }
        collisionEvent.ids.addAll(ids)
    }
}

private class CollisionInfo(
    val entity: Entity,
    val collisionArea: Rectangle,
    val solid: Boolean
)
