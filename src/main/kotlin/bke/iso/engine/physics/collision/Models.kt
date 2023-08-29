package bke.iso.engine.physics.collision

import bke.iso.engine.math.Box
import bke.iso.engine.world.Component
import bke.iso.engine.world.GameObject
import com.badlogic.gdx.math.Vector3

data class Collider (
    val size: Vector3,
    val offset: Vector3 = Vector3()
) : Component()

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
 * @property obj The colliding object
 * @property box The colliding object's collision box
 * @property distance The distance between the center point of both object's bounding boxes
 * @property side Which side the object collided with
 */
data class Collision(
    val obj: GameObject,
    val box: Box,
    val distance: Float,
    val side: CollisionSide
)

/**
 * Represents an object's collision with another object based on the other object's velocity.
 * @property obj The colliding object
 * @property box The colliding object's collision box
 * @property distance The distance between the center point of both object's bounding boxes
 * @property collisionTime A number between 0 and 1 representing the time the collision occurred within the entire frame
 * @property hitNormal A [Vector3] representing the collision normal
 * @property side Which side the object collided with
 */
data class PredictedCollision(
    val obj: GameObject,
    val box: Box,
    val distance: Float,
    val collisionTime: Float,
    val hitNormal: Vector3,
    val side: CollisionSide
)

data class SegmentCollision(
    val obj: GameObject,
    val distanceStart: Float,
    val distanceEnd: Float,
    val points: Set<Vector3>
)
