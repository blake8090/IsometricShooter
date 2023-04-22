package bke.iso.engine.physics

import bke.iso.engine.log
import bke.iso.engine.entity.Entity
import bke.iso.engine.math.getEdges
import bke.iso.engine.math.toVector2
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class CollisionData(
    val entity: Entity,
    val bounds: Bounds,
    // TODO: use a 3D box
    val box: Rectangle,
    val solid: Boolean,
)

// TODO: inner class of BoxCollision?
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

data class SegmentCollision(
    val data: CollisionData,
    val distanceFromStart: Float
)

class CollisionService(private val worldService: WorldService) : SingletonService {

    // TODO:
    //  - step thru dx and dy
    //  - ensure that solid collisions block other collisions behind that solid object!
    fun predictEntityCollisions(entity: Entity, dx: Float, dy: Float): EntityCollisionResult? {
        val collisionData = findCollisionData(entity) ?: return null
        val projection = Rectangle(collisionData.box)
        projection.x += dx
        projection.y += dy

        val collisions = worldService
            .findEntitiesInArea(projection)
            .asSequence()
            .filter { otherEntity -> otherEntity != entity }
            .mapNotNull(this::findCollisionData)
            .filter { data -> data.box.overlaps(projection) }
            .map { data ->
                BoxCollision(
                    data,
                    calculateCollisionSide(projection, data.box)
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

    fun checkSegmentCollisions(segment: Segment): List<SegmentCollision> {
        val collisions = mutableListOf<SegmentCollision>()
        val rect = getSegmentRectangle(segment.a, segment.b)
        worldService.findEntitiesInArea(rect)
            .mapNotNull(this::findCollisionData)
            .forEach { collisionData ->
                val points = findIntersectionPoints(segment, collisionData.box)
                if (points.isNotEmpty()) {
                    val pos = Vector2(collisionData.entity.x, collisionData.entity.y)
                    val distanceFromStart = segment.a.toVector2().dst(pos)
                    collisions.add(
                        SegmentCollision(
                            collisionData,
                            distanceFromStart
                        )
                    )
                }
            }
        return collisions.sortedBy(SegmentCollision::distanceFromStart)
    }

    private fun getSegmentRectangle(start: Vector3, end: Vector3): Rectangle {
        val min = Vector2(
            min(start.x, end.x),
            min(start.y, end.y)
        )
        val max = Vector2(
            max(start.x, end.x),
            max(start.y, end.y)
        )
        return Rectangle(
            min.x,
            min.y,
            max.x - min.x,
            max.y - min.y
        )
    }

    fun findIntersectionPoints(segment: Segment, rectangle: Rectangle): List<Vector3> {
        val points = mutableListOf<Vector3>()
        for (edge in rectangle.getEdges()) {
            val intersectionPoint = Vector2()
            val intersected = Intersector.intersectSegments(
                segment.a.toVector2(),
                segment.b.toVector2(),
                edge.a.toVector2(),
                edge.b.toVector2(),
                intersectionPoint
            )
            if (intersected) {
                points.add(Vector3(intersectionPoint.x, intersectionPoint.y, 0f))
            }
        }
        return points
    }
}
