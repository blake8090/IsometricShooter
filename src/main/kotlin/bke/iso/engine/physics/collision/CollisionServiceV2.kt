package bke.iso.engine.physics.collision

import bke.iso.engine.entity.Entity
import bke.iso.service.Singleton
import com.badlogic.gdx.math.Vector3

@Singleton
class CollisionServiceV2 {

    fun findCollisionData(entity: Entity): EntityCollisionData? {
        val collision = entity.get<CollisionV2>() ?: return null
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
}
