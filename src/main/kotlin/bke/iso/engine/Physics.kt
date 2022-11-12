package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.engine.entity.Component
import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import com.badlogic.gdx.math.Rectangle

data class Velocity(
    val dx: Float = 0f,
    val dy: Float = 0f
) : Component()

/**
 * Defines a collision box as a 2D rectangle.
 * @param x x position of the bottom-left corner of the box, relative to an entity's origin
 * @param y y position of the bottom-left corner of the box, relative to an entity's origin
 * @param width width of the box
 * @param length length of the box
 */
data class CollisionBox(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float,
    val length: Float
)

data class Collision(
    val box: CollisionBox,
    val static: Boolean = false
) : Component()

@Service
class Physics(private val entities: Entities) {
    fun update(deltaTime: Float) {
        entities.Search().withComponent(Velocity::class) { entity, velocity ->
            moveX(entity, velocity.dx * deltaTime)
            moveY(entity, velocity.dy * deltaTime)
            entity.removeComponent(Velocity::class)
        }
    }

    private fun moveX(entity: Entity, dx: Float) {
        if (dx == 0f) {
            return
        }

        val data = checkCollisionPath(entity, dx, 0f)
        if (data == null) {
            entity.setX(entity.getPos().x + dx)
            return
        }

        log.trace("collided with entity ${data.collidedEntity}")
        val x =
            if (data.collidedArea.x > data.area.x) {
                (data.collidedArea.x - data.area.width)
            } else {
                (data.collidedArea.x + data.collidedArea.width)
            }
        entity.setX(x)
    }

    private fun moveY(entity: Entity, dy: Float) {
        if (dy == 0f) {
            return
        }

        val data = checkCollisionPath(entity, 0f, dy)
        if (data == null) {
            entity.setY(entity.getPos().y + dy)
            return
        }

        log.trace("collided with entity ${data.collidedEntity}")
        if (data.collidedArea.y > data.area.y) {
            entity.setY(data.collidedArea.y - data.area.height)
        } else {
            entity.setY(data.collidedArea.y + data.collidedArea.height)
        }
    }

    private fun checkCollisionPath(entity: Entity, dx: Float, dy: Float): CollisionData? {
        val collisionArea = getCollisionArea(entity) ?: return null
        val projectedCollisionArea = Rectangle(collisionArea)
        projectedCollisionArea.x += dx
        projectedCollisionArea.y += dy

        val entitiesInArea = getEntitiesInArea(projectedCollisionArea)
            .filter { otherEntity -> otherEntity.id != entity.id }

        for (otherEntity in entitiesInArea) {
            val otherCollisionArea = getCollisionArea(otherEntity) ?: continue
            if (projectedCollisionArea.overlaps(otherCollisionArea)) {
                return CollisionData(projectedCollisionArea, otherEntity, otherCollisionArea)
            }
        }

        return null
    }

    private fun getCollisionArea(entity: Entity): Rectangle? {
        val collision = entity.getComponent<Collision>() ?: return null
        val pos = entity.getPos()
        return Rectangle(
            pos.x + collision.box.x,
            pos.y + collision.box.y,
            collision.box.width,
            collision.box.length
        )
    }

    // TODO: test negative coordinates, move to units?
    private fun getLocationsInArea(area: Rectangle): Set<Location> {
        val start = Location(area.x, area.y)
        val end = Location(
            area.x + area.width,
            area.y + area.height
        )

        val locations = mutableSetOf<Location>()
        for (x in start.x..end.x) {
            for (y in start.y..end.y) {
                locations.add(Location(x, y))
            }
        }
        return locations
    }

    // TODO: move this to entities.search
    private fun getEntitiesInArea(area: Rectangle): List<Entity> {
        val locations = getLocationsInArea(area)
//        log.trace("Found the following locations in area: $locations")
        return locations
            .flatMap(entities.Search()::atLocation)
            .map { id -> Entity(id, entities) }
    }

    private class CollisionData(
        val area: Rectangle,
        val collidedEntity: Entity,
        val collidedArea: Rectangle
    )
}
