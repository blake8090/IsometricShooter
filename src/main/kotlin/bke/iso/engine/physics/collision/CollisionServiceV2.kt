package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.log
import bke.iso.engine.math.Box
import bke.iso.engine.math.getEndPoint
import bke.iso.engine.render.debug.DebugRenderService
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.math.collision.Ray
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
        val movementRay = Ray(box.pos, Vector3(dx, dy, dz))
        debugRenderService.addLine(box.pos, movementRay.getEndPoint(1f), 1.5f, Color.ORANGE)

        val collisions = mutableSetOf<EntityBoxCollision>()
        for (other in entities) {
            val otherData = findCollisionData(other) ?: continue
            val minkowskiSum = box.minkowskiSum(otherData.box)
            debugRenderService.addBox(minkowskiSum, Color.ORANGE)

            val intersection = Vector3()
            val collided = Intersector.intersectRayBounds(
                movementRay,
                BoundingBox(minkowskiSum.min, minkowskiSum.max),
                intersection
            )

            if (collided && !intersection.isZero) {
                debugRenderService.addPoint(intersection, 3f, Color.YELLOW)

                if (box.pos.dst2(intersection) <= 0) {
                    val side = findSide(intersection, otherData.box)
                    collisions.add(
                        EntityBoxCollision(
                            other,
                            otherData,
                            side,
                            intersection,
                            box.pos.dst2(otherData.box.pos)
                        )
                    )
                }
            }
        }

        return PredictedCollisions(data, projectedBox, collisions)
    }

    private fun findSide(point: Vector3, box: Box): BoxCollisionSide {
        val top = Vector3(box.center).add(0f, 0f, box.height / 2f)
        val bottom = Vector3(box.center).sub(0f, 0f, box.height / 2f)
        val left = Vector3(box.center).sub(box.width / 2f, 0f, 0f)
        val right = Vector3(box.center).add(box.width / 2f, 0f, 0f)
        val front = Vector3(box.center).sub(0f, box.length / 2f, 0f)
        val back = Vector3(box.center).add(0f, box.length / 2f, 0f)

        val dTop = top.dst2(point)
        val dBottom = bottom.dst2(point)
        val dLeft = left.dst2(point)
        val dRight = right.dst2(point)
        val dFront = front.dst2(point)
        val dBack = back.dst2(point)

        log.trace("dst top: $dTop bottom: $dBottom left: $dLeft right: $dRight front: $dFront back: $dBack")
        val dx = dLeft + dRight
        val dy = dFront + dBack
        val dz = dTop + dBottom
        log.trace("dst x: $dx, y: $dy, z: $dz")

        data class Entry(
            val dist: Float,
            val distAxis: Float,
            val side: BoxCollisionSide
        )
        val entries = listOf(
            Entry(dTop, dz, BoxCollisionSide.TOP),
            Entry(dBottom, dz, BoxCollisionSide.BOTTOM),
            Entry(dLeft, dx, BoxCollisionSide.LEFT),
            Entry(dRight, dx, BoxCollisionSide.RIGHT),
            Entry(dFront, dy, BoxCollisionSide.FRONT),
            Entry(dBack, dy, BoxCollisionSide.BACK)
        )
        // sorting by multiple distances handles cases where two axes have the same distance.
        // for example, if dx == dy, then take the minimum of dLeft, dRight, dFront, and dBack.
        val min = entries.minWith(
            Comparator.comparing(Entry::distAxis)
                .thenBy(Entry::dist)
        )
        log.trace("min: $min")
        return min.side
    }

    // TODO: move this to Box.kt?
    private fun boxesIntersect(a: Box, b: Box): Boolean {
        // TODO: dont need these vals anymore
        val aMin = a.min
        val aMax = a.max
        val bMin = b.min
        val bMax = b.max
        return aMin.x <= bMax.x &&
                aMax.x >= bMin.x &&
                aMin.y <= bMax.y &&
                aMax.y >= bMin.y &&
                aMin.z <= bMax.z &&
                aMax.z >= bMin.z
    }

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
