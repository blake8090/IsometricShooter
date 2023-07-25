package bke.iso.engine.physics.collision

import bke.iso.engine.event.Event
import bke.iso.engine.math.Box
import bke.iso.engine.world.WorldObject
import com.badlogic.gdx.math.Vector3

data class CollisionEvent(
    val obj: WorldObject,
    val collision: ObjectCollision
) : Event()

data class CollisionData(
    val box: Box,
    val solid: Boolean
)

enum class BoxCollisionSide {
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
 * @property obj The colliding object
 * @property data The colliding object's [CollisionData]
 * @property distance The distance between the center point of both object's bounding boxes
 * @property side Which side the object collided with
 */
data class ObjectCollision(
    val obj: WorldObject,
    val data: CollisionData,
    val distance: Float,
    val side: BoxCollisionSide
)

/**
 * Represents an object's collision with another object based on the other object's velocity.
 * @property obj The colliding object
 * @property data The colliding object's [CollisionData]
 * @property distance The distance between the center point of both object's bounding boxes
 * @property collisionTime A number between 0 and 1 representing the time the collision occurred within the entire frame
 * @property hitNormal A [Vector3] representing the collision normal
 * @property side Which side the object collided with
 */
data class PredictedObjectCollision(
    val obj: WorldObject,
    val data: CollisionData,
    val distance: Float,
    val collisionTime: Float,
    val hitNormal: Vector3,
    val side: BoxCollisionSide
)

data class ObjectSegmentCollision(
    val obj: WorldObject,
    val data: CollisionData,
    val distanceStart: Float,
    val distanceEnd: Float,
    val points: Set<Vector3>
)
