package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.math.Box
import bke.iso.engine.world.WorldObject
import com.badlogic.gdx.math.Vector3

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

data class BoxCollision(
    val obj: WorldObject,
    val data: CollisionData,
    val side: BoxCollisionSide,
    val distance: Float,
    val collisionTime: Float,
    val hitNormal: Vector3
)

data class PredictedCollisions(
    val data: CollisionData,
    val projectedBox: Box,
    val collisions: Set<BoxCollision>
)

data class EntitySegmentCollision(
    val entity: Entity,
    val data: CollisionData,
    val distanceFromStart: Float,
    val distanceFromEnd: Float,
    val intersectionPoints: Set<Vector3>
)
