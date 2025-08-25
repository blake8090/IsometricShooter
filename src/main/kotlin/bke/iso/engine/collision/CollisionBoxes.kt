package bke.iso.engine.collision

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.math.Box
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.event.EntityComponentAdded
import bke.iso.engine.world.event.EntityComponentRemoved
import bke.iso.engine.world.event.EntityCreated
import bke.iso.engine.world.event.EntityDeleted
import bke.iso.engine.world.event.EntityMoved
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ObjectMap

/**
 * Provides efficient access to entity collision boxes with automatic caching.
 *
 * This class automatically caches collision box calculations and invalidates
 * the cache when entities move or their [Collider] components change.
 */
class CollisionBoxes : EngineModule() {

    override val moduleName = "collisionBoxes"
    override val updateWhileLoading = false
    override val profilingEnabled = false

    private val cachedBoxes = ObjectMap<Entity, Box>()
    private val valid = ObjectMap<Entity, Boolean>()

    override fun handleEvent(event: Event) {
        when (event) {
            is EntityCreated -> {
                invalidateBox(event.entity)
            }

            is EntityDeleted -> {
                cachedBoxes.remove(event.entity)
                valid.remove(event.entity)
            }

            is EntityMoved -> {
                invalidateBox(event.entity)
            }

            is EntityComponentAdded -> {
                if (event.component is Collider) {
                    invalidateBox(event.entity)
                }
            }

            is EntityComponentRemoved -> {
                if (event.component is Collider) {
                    invalidateBox(event.entity)
                }
            }
        }
    }

    private fun invalidateBox(entity: Entity) {
        valid.put(entity, false)
    }

    operator fun get(entity: Entity): Box? {
        val collider = entity.get<Collider>() ?: return null

        val cached = cachedBoxes.get(entity)
        if (cached != null && valid.get(entity)) {
            return cached
        }

        val box = calculateBox(entity, collider)
        cachedBoxes.put(entity, box)
        valid.put(entity, true)
        return box
    }

    private fun calculateBox(entity: Entity, collider: Collider): Box {
        val min = entity.pos.add(collider.offset)
        val max = Vector3(min).add(collider.size)
        return Box.fromMinMax(min, max)
    }
}
