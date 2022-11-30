package bke.iso.engine.physics

import bke.iso.app.service.Service
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.EntityService
import com.badlogic.gdx.math.Polygon

data class CollisionData(
    val entity: Entity,
    val bounds: Bounds,
    val area: Polygon,
    val solid: Boolean
)

@Service
class CollisionService(private val entityService: EntityService) {
    /**
     * Returns the [CollisionData] for an [Entity].
     * If the [Entity] does not have a [Collision] component, null will be returned.
     */
    fun findCollisionData(entity: Entity): CollisionData? {
        val collision = entity.get<Collision>() ?: return null
        val bounds = collision.bounds
        return CollisionData(
            entity,
            bounds,
            calculateCollisionArea(entity, collision),
            collision.solid
        )
    }

    private fun calculateCollisionArea(entity: Entity, collision: Collision): Polygon {
        val bounds = collision.bounds
        val w = bounds.width
        val l = bounds.length
        val area = Polygon(
            floatArrayOf(
                0f, 0f,
                0f, l,
                w, l,
                w, 0f
            )
        )
        area.setPosition(
            entity.x + bounds.offsetX,
            entity.y + bounds.offsetY
        )
        return area
    }
}
