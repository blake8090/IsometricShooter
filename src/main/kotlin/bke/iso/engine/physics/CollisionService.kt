package bke.iso.engine.physics

import bke.iso.app.service.Service
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.log
import com.badlogic.gdx.math.Rectangle
import java.util.Collections.min

data class CollisionData(
    val entity: Entity,
    val bounds: Bounds,
    val area: Rectangle,
    val solid: Boolean,
)

data class CollisionResult(
    val collisionData: CollisionData,
    val collisions: Set<CollisionData>,
    val solidCollisions: Set<CollisionData>
)

enum class CollisionSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CORNER
}

@Service
class CollisionService(private val entityService: EntityService) {
    // TODO: ensure that solid collisions block other collisions behind that solid object!
    fun checkCollisions(entity: Entity): CollisionResult? {
        val collisionData = findCollisionData(entity) ?: return null
        val collisions = entityService.search
            .inArea(collisionData.area)
            .mapNotNull(this::findCollisionData)
            .filter { otherData -> collided(collisionData, otherData) }

        return CollisionResult(
            collisionData,
            collisions.filter { data -> !data.solid }.toSet(),
            collisions.filter { data -> data.solid }.toSet()
        )
    }

    private fun collided(collisionData: CollisionData, other: CollisionData): Boolean {
        if (collisionData.entity == other.entity) {
            return false
        }
        val area = collisionData.area
        val secondArea = other.area
        val side = calculateCollisionSide(area, secondArea)
        log.trace("collided on side $side")
        return area.overlaps(secondArea)
    }

    private fun calculateCollisionSide(area: Rectangle, secondArea: Rectangle): CollisionSide {
        val deltaLeft = (secondArea.x + secondArea.width) - area.x
        val deltaRight = (area.x + area.width) - secondArea.x
        val deltaTop = (area.y + area.height) - secondArea.y
        val deltaBottom = (secondArea.y + secondArea.height) - area.y
        log.trace("deltaLeft: $deltaLeft, deltaRight: $deltaRight, deltaTop: $deltaTop, deltaBottom: $deltaBottom")

        return when (min(listOf(deltaLeft, deltaRight, deltaTop, deltaBottom))) {
            deltaLeft -> CollisionSide.LEFT
            deltaRight -> CollisionSide.RIGHT
            deltaTop -> CollisionSide.TOP
            deltaBottom -> CollisionSide.BOTTOM
            else -> CollisionSide.CORNER
        }
    }

    /**
     * Returns the [CollisionData] for an [Entity].
     * If the [Entity] does not have a [Collision] component, null will be returned.
     */
    fun findCollisionData(entity: Entity): CollisionData? {
        val collision = entity.get<Collision>() ?: return null
        val bounds = collision.bounds
        return CollisionData(
            entity,
            bounds,
            calculateCollisionArea(entity, bounds),
            collision.solid
        )
    }

    private fun calculateCollisionArea(entity: Entity, bounds: Bounds): Rectangle {
        return Rectangle(
            entity.x + bounds.offsetX,
            entity.y + bounds.offsetY,
            bounds.width,
            bounds.length
        )
    }
}
