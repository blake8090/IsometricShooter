package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.math.Box
import bke.iso.engine.render.debug.DebugRenderService
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import kotlin.math.abs
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
        val collisions = mutableSetOf<EntityBoxCollision>()
        for (other in entities) {
            val otherData = findCollisionData(other) ?: continue
            val minowskiSum = box.minowskiSum(otherData.box)
            debugRenderService.addBox(minowskiSum, Color.ORANGE)
            //val collisionSide = checkCollision(projectedBox, otherData.box) ?: continue
            //collisions.add(EntityBoxCollision(other, otherData, collisionSide))
        }

        return PredictedCollisions(data, projectedBox, collisions)
    }

    private fun checkCollision(box: Box, box2: Box): BoxCollisionSide? {
        if (!boxesIntersect(box, box2)) {
            return null
        }

        val diffX = abs(box.pos.x - box2.pos.x)
        val diffY = abs(box.pos.y - box2.pos.y)
        val diffZ = abs(box.pos.z - box2.pos.z)
        return when (maxOf(diffX, diffY, diffZ)) {
            diffX -> {
                if (box.pos.x - box2.pos.x > 0) {
                    BoxCollisionSide.RIGHT
                } else {
                    BoxCollisionSide.LEFT
                }
            }

            diffY -> {
                if (box.pos.y - box2.pos.y > 0) {
                    BoxCollisionSide.BACK
                } else {
                    BoxCollisionSide.FRONT
                }
            }

            diffZ -> {
                if (box.pos.z - box2.pos.z > 0) {
                    BoxCollisionSide.TOP
                } else {
                    BoxCollisionSide.BOTTOM
                }
            }

            else -> BoxCollisionSide.CORNER
        }
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
