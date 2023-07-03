package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.math.Box
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
    BOTTOM,
    CORNER
}

data class EntityBoxCollision(
    val entity: Entity,
    val data: EntityCollisionData,
    val side: BoxCollisionSide,
    val intersection: Vector3,
    val distance: Float
)

data class PredictedCollisions(
    val data: EntityCollisionData,
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
