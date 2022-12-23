package bke.iso.engine.physics

import bke.iso.app.service.Service
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import bke.iso.engine.log
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.abs

data class CollisionData(
    val entity: Entity,
    val bounds: Bounds,
    val area: Rectangle,
    val solid: Boolean,
)

enum class CollisionSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CORNER
}

data class BoxCollision(
    val data: CollisionData,
    val side: CollisionSide
)

data class EntityCollisionResult(
    val bounds: Bounds,
    val box: Rectangle,
    val collisions: Set<BoxCollision>
)

@Service
class CollisionService(private val entityService: EntityService) {

    // TODO:
    //  - step thru dx and dy
    //  - ensure that solid collisions block other collisions behind that solid object!
    fun predictEntityCollisions(entity: Entity, dx: Float, dy: Float): EntityCollisionResult? {
        val collisionData = findCollisionData(entity) ?: return null
        val projection = Rectangle(collisionData.area)
        projection.x += dx
        projection.y += dy

        val collisions = entityService.search
            .inArea(projection)
            .asSequence()
            .filter { otherEntity -> otherEntity != entity }
            .mapNotNull(this::findCollisionData)
            .filter { data -> data.area.overlaps(projection) }
            .map { data ->
                BoxCollision(
                    data,
                    calculateCollisionSide(projection, data.area)
                )
            }
            .toSet()

        return if (collisions.isNotEmpty()) {
            EntityCollisionResult(
                collisionData.bounds,
                projection,
                collisions
            )
        } else {
            null
        }
    }

    fun calculateCollisionSide(area: Rectangle, secondArea: Rectangle): CollisionSide {
//        val deltaLeft = (secondArea.x + secondArea.width) - area.x
//        val deltaRight = (area.x + area.width) - secondArea.x
//        val deltaTop = (area.y + area.height) - secondArea.y
//        val deltaBottom = (secondArea.y + secondArea.height) - area.y
//        log.trace("deltaLeft: $deltaLeft, deltaRight: $deltaRight, deltaTop: $deltaTop, deltaBottom: $deltaBottom")

        // TODO: add extension to clean this up
        val center = Vector2()
        val secondCenter = Vector2()
        area.getCenter(center)
        secondArea.getCenter(secondCenter)
        val diffX = abs(center.x - secondCenter.x)
        val diffY = abs(center.y - secondCenter.y)
        log.trace("diffX: $diffX, diffY: $diffY")

        return if (diffX > diffY) {
            getCollisionSideX(area, secondArea)
        } else if (diffY > diffX) {
            getCollisionSideY(area, secondArea)
        } else {
            CollisionSide.CORNER
        }

//        return when (min(listOf(deltaLeft, deltaRight, deltaTop, deltaBottom))) {
//            deltaLeft -> CollisionSide.LEFT
//            deltaRight -> CollisionSide.RIGHT
//            deltaTop -> CollisionSide.TOP
//            deltaBottom -> CollisionSide.BOTTOM
//            else -> CollisionSide.CORNER
//        }
    }

    private fun getCollisionSideX(area: Rectangle, secondArea: Rectangle): CollisionSide {
        val deltaLeft = (secondArea.x + secondArea.width) - area.x
        val deltaRight = (area.x + area.width) - secondArea.x
        return if (deltaLeft < deltaRight) {
            CollisionSide.LEFT
        } else {
            CollisionSide.RIGHT
        }
    }

    private fun getCollisionSideY(area: Rectangle, secondArea: Rectangle): CollisionSide {
        val deltaTop = (area.y + area.height) - secondArea.y
        val deltaBottom = (secondArea.y + secondArea.height) - area.y
        return if (deltaTop < deltaBottom) {
            CollisionSide.TOP
        } else {
            CollisionSide.BOTTOM
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
