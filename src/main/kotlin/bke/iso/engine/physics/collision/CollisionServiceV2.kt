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
import kotlin.math.max
import kotlin.math.min

class CollisionServiceV2(
    private val worldService: WorldService,
    private val debugRenderService: DebugRenderService
) : SingletonService {

    fun findCollisionData(entity: Entity): EntityCollisionData? {
        val collision = entity.get<Collider>() ?: return null
        val bounds = collision.bounds
        val box = Box(
            Vector3(
                entity.x + bounds.offset.x,
                entity.y + bounds.offset.y,
                entity.z + bounds.offset.z
            ),
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

        // broad-phase: find other entities along movement path by projecting the entity's collision box
        val px = if (dx < 0) floor(dx) else ceil(dx)
        val py = if (dy < 0) floor(dy) else ceil(dy)
        val pz = if (dz < 0) floor(dz) else ceil(dz)
        val projectedBox = box.project(px, py, pz)
        debugRenderService.addBox(projectedBox, Color.ORANGE)

        val entities = findEntitiesInArea(projectedBox)
            .filter { otherEntity -> otherEntity != entity }

        // narrow-phase: check for collisions with each entity along movement path
        val collisions = mutableSetOf<EntityBoxCollision>()
        for (other in entities) {
            val otherData = findCollisionData(other) ?: continue

            val collisionTime = checkSweptCollision(box, Vector3(dx, dy, dz), otherData.box)
            if (collisionTime < 1f) {
                val remainingTime = 1f - collisionTime
                log.trace("remaining time: $remainingTime")
                collisions.add(
                    EntityBoxCollision(
                        other,
                        otherData,
                        BoxCollisionSide.BOTTOM,
                        Vector3(),
                        0f,
                        collisionTime
                    )
                )
            }
        }

        return PredictedCollisions(data, projectedBox, collisions)
    }

    private fun checkSweptCollision(box: Box, delta: Vector3, other: Box): Float {
        val pBox = box.project(delta.x, delta.y, delta.z)
        if (!boxesIntersect(pBox, other)) {
            return 1f
        }
        log.trace("begin swept collision check")
        val dxEntry =
            if (delta.x > 0f) {
                other.min.x - box.max.x
            } else {
                other.max.x - box.min.x
            }
        val dxExit =
            if (delta.x > 0f) {
                other.max.x - box.min.x
            } else {
                other.min.x - box.max.x
            }

        val dyEntry =
            if (delta.y > 0f) {
                other.min.y - box.max.y
            } else {
                other.max.y - box.min.y
            }
        val dyExit =
            if (delta.y > 0f) {
                other.max.y - box.min.y
            } else {
                other.min.y - box.max.y
            }

//        log.trace("xEntry: $dxEntry, dxExit: $dxExit, dyEntry: $dyEntry, dyExit: $dyExit")

        val txEntry =
            if (delta.x == 0f) {
                Float.NEGATIVE_INFINITY
            } else {
                dxEntry / delta.x
            }
        val txExit =
            if (delta.x == 0f) {
                Float.POSITIVE_INFINITY
            } else {
                dxExit / delta.x
            }

        val tyEntry =
            if (delta.y == 0f) {
                Float.NEGATIVE_INFINITY
            } else {
                dyEntry / delta.y
            }
        val tyExit =
            if (delta.y == 0f) {
                Float.POSITIVE_INFINITY
            } else {
                dyExit / delta.y
            }

//        log.trace("txEntry: $txEntry, txExit: $txExit")
//        log.trace("tyEntry: $tyEntry, tyExit: $tyExit")

        val entryTime = max(txEntry, tyEntry)
        val exitTime = min(txExit, tyExit)

        val e =
            if (entryTime > exitTime || (txEntry < 0f && tyEntry < 0f) || txEntry > 1f || tyEntry > 1f) {
                1f
            } else {
                entryTime
            }
        log.trace("entry time: $e")
        log.trace("end swept collision check")
        return e
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

//    private data class SideResult(
//        val side: BoxCollisionSide,
//        val dot: Float,
//        val dist: Float
//    )

//    private fun checkMinkowski(box: Box, delta: Vector3, other: Entity, otherData: EntityCollisionData) {
//        val movementRay = Ray(box.pos, delta)
//        debugRenderService.addLine(box.pos, movementRay.getEndPoint(1f), 1.5f, Color.ORANGE)
//
//        val minkowskiSum = box.minkowskiSum(otherData.box)
//        debugRenderService.addBox(minkowskiSum, Color.ORANGE)
//        val intersection = Vector3()
//        val collided = Intersector.intersectRayBounds(
//            movementRay,
//            BoundingBox(minkowskiSum.min, minkowskiSum.max),
//            intersection
//        )
//
//        val collisions = mutableSetOf<EntityBoxCollision>()
//        if (collided && !intersection.isZero) {
//            debugRenderService.addPoint(intersection, 3f, Color.YELLOW)
//            if (box.pos.dst2(intersection) <= 0f) {
//                val result = findSide(movementRay, intersection, otherData.box)
//                log.trace("adding collision with side: ${result.side}, dist: ${result.dist}")
//                collisions.add(
//                    EntityBoxCollision(
//                        other,
//                        otherData,
//                        result.side,
//                        intersection,
//                        result.dist,
//                        0f
//                    )
//                )
//            }
//        }
//    }

//    private fun findSide(ray: Ray, intersection: Vector3, box: Box): SideResult {
//        fun dot(x: Float, y: Float, z: Float) =
//            ray.direction.dot(Vector3(x, y, z))
//
//        val topPoint = Vector3(box.center).add(0f, 0f, box.height / 2f)
//        val bottomPoint = Vector3(box.center).sub(0f, 0f, box.height / 2f)
//        val leftPoint = Vector3(box.center).sub(box.width / 2f, 0f, 0f)
//        val rightPoint = Vector3(box.center).add(box.width / 2f, 0f, 0f)
//        val frontPoint = Vector3(box.center).sub(0f, box.length / 2f, 0f)
//        val backPoint = Vector3(box.center).add(0f, box.length / 2f, 0f)
//
//        val entries = listOf(
//            SideResult(BoxCollisionSide.TOP, dot(0f, 0f, 1f), topPoint.dst(intersection)),
//            SideResult(BoxCollisionSide.BOTTOM, dot(0f, 0f, -1f), bottomPoint.dst(intersection)),
//            SideResult(BoxCollisionSide.LEFT, dot(-1f, 0f, 0f), leftPoint.dst(intersection)),
//            SideResult(BoxCollisionSide.RIGHT, dot(1f, 0f, 0f), rightPoint.dst(intersection)),
//            SideResult(BoxCollisionSide.FRONT, dot(0f, -1f, 0f), frontPoint.dst(intersection)),
//            SideResult(BoxCollisionSide.BACK, dot(0f, 1f, 0f), backPoint.dst(intersection))
//        )
//
//        log.trace("entries: ${entries.joinToString("\n")}")
//
//        return entries.filter { it.dot < 0 }
//            .minBy { it.dist }
//    }

    private fun findEntitiesInArea(box: Box): Set<Entity> {
        val minX = box.min.x.toInt()
        val maxX = box.max.x.toInt()

        val minY = box.min.y.toInt()
        val maxY = box.max.y.toInt()

        val minZ = box.min.z.toInt()
        val maxZ = box.max.z.toInt()

        val entities = mutableSetOf<Entity>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    worldService.getObjectsAt(x, y, z)
                        .filterIsInstance<Entity>()
                        .forEach(entities::add)
                }
            }
        }
        return entities
    }
}
