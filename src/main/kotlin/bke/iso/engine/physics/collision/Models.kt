package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import com.badlogic.gdx.math.Vector3

data class EntityCollisionData(
    val bounds: Bounds,
    val box: Box,
    val solid: Boolean
)

enum class BoxCollisionSide {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM
}

data class EntityBoxCollision(
    val entity: Entity,
    val data: EntityCollisionData,
    val side: BoxCollisionSide
)

data class PredictedCollisions(
    val projectedBox: Box,
    val collisions: Set<EntityBoxCollision>
)

data class EntitySegmentCollision(
    val entity: Entity,
    val data: EntityCollisionData,
    val distanceFromStart: Float,
    val distanceFromEnd: Float,
    val intersectionPoints: Set<Vector3>
)
