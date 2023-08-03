package bke.iso.v2.engine.physics

import bke.iso.engine.entity.Component
import bke.iso.engine.physics.BoxCollisionSide
import bke.iso.engine.physics.CollisionData
import bke.iso.v2.engine.world.GameObject
import com.badlogic.gdx.math.Vector3

data class FrameCollisions(
    val collisions: MutableSet<Collision> = mutableSetOf()
) : Component()

/**
 * Contains details on an object's collision on another object.
 * @property obj The colliding object
 * @property data The colliding object's [CollisionData]
 * @property distance The distance between the center point of both object's bounding boxes
 * @property side Which side the object collided with
 */
data class Collision(
    val obj: GameObject,
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
data class PredictedCollision(
    val obj: GameObject,
    val data: CollisionData,
    val distance: Float,
    val collisionTime: Float,
    val hitNormal: Vector3,
    val side: BoxCollisionSide
)

data class SegmentCollision(
    val obj: GameObject,
    val data: CollisionData,
    val distanceStart: Float,
    val distanceEnd: Float,
    val points: Set<Vector3>
)
