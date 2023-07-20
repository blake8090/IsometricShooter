package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.log
import bke.iso.engine.math.Box
import bke.iso.engine.render.debug.DebugRenderService
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlin.math.ceil
import kotlin.math.floor

class CollisionServiceV2(
    private val worldService: WorldService,
    private val debugRenderService: DebugRenderService
) : SingletonService {

    fun findCollisionData(entity: Entity): EntityCollisionData? {
        val collision = entity.get<Collider>() ?: return null
        val bounds = collision.bounds
        val box = Box(
            entity.pos.add(bounds.offset),
            bounds.dimensions.x,
            bounds.dimensions.y,
            bounds.dimensions.z
        )

        return EntityCollisionData(
            bounds,
            box,
            collision.solid
        )
    }

    fun predictEntityCollisions(entity: Entity, dx: Float, dy: Float, dz: Float): PredictedCollisions? {
        val data = findCollisionData(entity) ?: return null
        val box = data.box

        // broad-phase: instead of iterating through every entity, only check entities within general area of movement
        val px = if (dx < 0) floor(dx) else ceil(dx)
        val py = if (dy < 0) floor(dy) else ceil(dy)
        val pz = if (dz < 0) floor(dz) else ceil(dz)
        val projectedBox = box.project(px, py, pz)
        debugRenderService.addBox(projectedBox, Color.ORANGE)

        val entities = findEntitiesInArea(projectedBox)
            .filter { otherEntity -> otherEntity != entity }

        // narrow-phase: check precise collisions for each entity within area
        val collisions = mutableSetOf<EntityBoxCollision>()
        for (other in entities) {
            val otherData = findCollisionData(other) ?: continue

            checkSweptCollision(box, Vector3(dx, dy, dz), otherData.box)?.let { collision ->
                val distance = box.center.dst(otherData.box.center)
                val side = getCollisionSide(collision.hitNormal)
                log.trace("dist: $distance, collision time: ${collision.collisionTime}, hit normal: ${collision.hitNormal}, side: $side")

                collisions.add(
                    EntityBoxCollision(other, otherData, side, distance, collision.collisionTime, collision.hitNormal)
                )
            }
        }

        return PredictedCollisions(data, projectedBox, collisions)
    }

    private data class SweptCollision(
        val collisionTime: Float,
        val hitNormal: Vector3
    )

    private fun checkSweptCollision(a: Box, delta: Vector3, b: Box): SweptCollision? {
        // if box B is not in the projection of box A, both boxes will never collide
        val pBox = a.project(delta.x, delta.y, delta.z)
        if (!boxesIntersect(pBox, b)) {
            return null
        }

        /**
         * Represents the distance between the nearest sides of boxes A and B.
         * Box A would need to travel this distance to begin overlapping box B.
         */
        val entryDistanceX =
            if (delta.x > 0f) {
                b.min.x - a.max.x
            } else {
                b.max.x - a.min.x
            }

        /**
         * Represents the time it will take for box A to begin overlapping box B.
         * Calculated using (distance / speed).
         */
        val entryTimeX =
            if (delta.x != 0f) {
                entryDistanceX / delta.x
            } else {
                Float.NEGATIVE_INFINITY
            }

        /**
         * Represents the distance between the farthest sides of boxes A and B.
         * Box A would need to travel this distance to stop overlapping box B.
         */
        val exitDistanceX =
            if (delta.x > 0f) {
                b.max.x - a.min.x
            } else {
                b.min.x - a.max.x
            }

        /**
         * Represents the time it will take for box A to stop overlapping box B.
         * Calculated using (distance / speed).
         */
        val exitTimeX =
            if (delta.x != 0f) {
                exitDistanceX / delta.x
            } else {
                Float.POSITIVE_INFINITY
            }

        // y-axis
        val entryDistanceY =
            if (delta.y > 0f) {
                b.min.y - a.max.y
            } else {
                b.max.y - a.min.y
            }

        val entryTimeY =
            if (delta.y != 0f) {
                entryDistanceY / delta.y
            } else {
                Float.NEGATIVE_INFINITY
            }

        val exitDistanceY =
            if (delta.y > 0f) {
                b.max.y - a.min.y
            } else {
                b.min.y - a.max.y
            }

        val exitTimeY =
            if (delta.y != 0f) {
                exitDistanceY / delta.y
            } else {
                Float.POSITIVE_INFINITY
            }

        // z-axis
        val entryDistanceZ =
            if (delta.z > 0f) {
                b.min.z - a.max.z
            } else {
                b.max.z - a.min.z
            }

        val entryTimeZ =
            if (delta.z != 0f) {
                entryDistanceZ / delta.z
            } else {
                Float.NEGATIVE_INFINITY
            }

        val exitDistanceZ =
            if (delta.z > 0f) {
                b.max.z - a.min.z
            } else {
                b.min.z - a.max.z
            }

        val exitTimeZ =
            if (delta.z != 0f) {
                exitDistanceZ / delta.z
            } else {
                Float.POSITIVE_INFINITY
            }

        // if the time ranges on both axes never overlap, there's no collision
        if ((entryTimeX > exitTimeY && entryTimeX > exitTimeZ) ||
            (entryTimeY > exitTimeX && entryTimeY > exitTimeZ) ||
            (entryTimeZ > exitTimeX && entryTimeZ > exitTimeY)
        ) {
            return null
        }

        // find the longest entry time. the shortest entry time only demonstrates a collision on one axis.
        val entryTime = maxOf(entryTimeX, entryTimeY, entryTimeZ)

        // if the entryTime is not within the 0-1 range, that means no collision occurred
        if (entryTime !in 0f..1f) {
            return null
        }

        var hitNormalX = 0f
        var hitNormalY = 0f
        var hitNormalZ = 0f
        if (entryTimeX > entryTimeY && entryTimeX > entryTimeZ) {
            hitNormalX = if (delta.x > 0) -1f else 1f
        } else if (entryTimeY > entryTimeX && entryTimeY > entryTimeZ) {
            hitNormalY = if (delta.y > 0) -1f else 1f
        } else {
            hitNormalZ = if (delta.z > 0) -1f else 1f
        }

        /**
         * The hit normal points either up, down, left, right, top, or bottom.
         * Box B would need to push box A in this direction to stop box A from moving.
         * Using this, the collision direction can be determined.
         */
        val hitNormal = Vector3(hitNormalX, hitNormalY, hitNormalZ)
        return SweptCollision(entryTime, hitNormal)
    }

    private fun getCollisionSide(hitNormal: Vector3): BoxCollisionSide =
        when (hitNormal) {
            Vector3(-1f, 0f, 0f) -> BoxCollisionSide.LEFT
            Vector3(1f, 0f, 0f) -> BoxCollisionSide.RIGHT
            Vector3(0f, -1f, 0f) -> BoxCollisionSide.FRONT
            Vector3(0f, 1f, 0f) -> BoxCollisionSide.BACK
            Vector3(0f, 0f, -1f) -> BoxCollisionSide.BOTTOM
            Vector3(0f, 0f, 1f) -> BoxCollisionSide.TOP
            else -> BoxCollisionSide.CORNER
        }

    // TODO: move this to Box.kt?
    private fun boxesIntersect(a: Box, b: Box): Boolean {
        return a.min.x < b.max.x &&
                a.max.x > b.min.x &&
                a.min.y < b.max.y &&
                a.max.y > b.min.y &&
                a.min.z < b.max.z &&
                a.max.z > b.min.z
    }

    private fun findEntitiesInArea(box: Box): Set<Entity> {
        val minX = box.min.x.toInt()
        val maxX = box.max.x.toInt()

        val minY = box.min.y.toInt()
        val maxY = box.max.y.toInt()

        val maxZ = box.max.z.toInt()

        val entities = mutableSetOf<Entity>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in 0..maxZ) {
                    worldService.getObjectsAt(x, y, z)
                        .filterIsInstance<Entity>()
                        .forEach(entities::add)
                }
            }
        }
        return entities
    }
}
