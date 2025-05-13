package bke.iso.engine.collision

import bke.iso.engine.math.Box
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3

enum class CollisionSide {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    CORNER
}

/**
 * Contains details on an object's collision on another object.
 * @property entity The colliding object
 * @property box The colliding object's collision box
 * @property distance The distance between the center point of both object's bounding boxes
 * @property side Which side the object collided with
 */
data class Collision(
    val entity: Entity,
    val box: Box,
    val distance: Float,
    val side: CollisionSide
)

/**
 * Represents an object's collision with another object based on the other object's velocity.
 * @property entity The colliding object
 * @property box The colliding object's collision box
 * @property distance The distance between the center point of both object's bounding boxes
 * @property collisionTime A number between 0 and 1 representing the time the collision occurred within the entire frame
 * @property hitNormal A [Vector3] representing the collision normal
 * @property side Which side the object collided with
 */
data class PredictedCollision(
    val entity: Entity,
    val box: Box,
    val distance: Float,
    val collisionTime: Float,
    val hitNormal: Vector3,
    val side: CollisionSide
)

data class SegmentCollision(
    val entity: Entity,
    val distanceStart: Float,
    val distanceEnd: Float,
    val points: Set<Vector3>
)

data class PointCollision(
    val entity: Entity,
    val box: Box
)
