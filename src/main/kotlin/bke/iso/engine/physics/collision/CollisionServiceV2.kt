package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService
import com.badlogic.gdx.math.Vector3
import kotlin.math.ceil
import kotlin.math.floor

class CollisionServiceV2(private val worldService: WorldService) : SingletonService {

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
        val projectedBox = Box(
            box.center,
            box.width + dx,
            box.length + dy,
            box.height + dz
        )

        val entities = findEntitiesInArea(projectedBox)
            .filter { otherEntity -> otherEntity != entity }

        val collisions = mutableSetOf<EntityBoxCollision>()
        for (other in entities) {
            val otherData = findCollisionData(other) ?: continue
            val collisionSide = checkCollision(projectedBox, otherData.box) ?: continue
            collisions.add(EntityBoxCollision(other, otherData, collisionSide))
        }

        return PredictedCollisions(projectedBox, collisions)
    }

    private fun checkCollision(box: Box, box2: Box): BoxCollisionSide? {
        if (!boxesIntersect(box, box2)) {
            return null
        }
        return BoxCollisionSide.TOP
    }

    // TODO: move this to Box.kt?
    private fun boxesIntersect(a: Box, b: Box): Boolean {
        val aMin = a.getMin()
        val aMax = a.getMax()
        val bMin = b.getMin()
        val bMax = b.getMax()
        return aMin.x <= bMax.x &&
                aMax.x >= bMin.x &&
                aMin.y <= bMax.y &&
                aMax.y >= bMin.y &&
                aMin.z <= bMax.z &&
                aMax.z >= bMin.z
    }

    private fun findEntitiesInArea(box: Box): Set<Entity> {
        val min = box.getMin()
        val max = box.getMax()

        val minX = floor(min.x).toInt()
        val maxX = ceil(max.x).toInt()

        val minY = floor(min.y).toInt()
        val maxY = ceil(max.y).toInt()

        // TODO: optimize this to avoid searching all entities from the ground up
        val minZ = 0
        val maxZ = ceil(max.z).toInt()

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
