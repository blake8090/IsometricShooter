package bke.iso.engine.entity

import bke.iso.app.service.Service
import bke.iso.engine.Location
import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.reflect.KClass

@Service
class Entities {
    private val posById = mutableMapOf<UUID, Vector2>()
    private val locations = EntityLocations()

    val components = EntityComponents()

    fun create(x: Float = 0f, y: Float = 0f): Entity {
        val id = UUID.randomUUID()
        setPos(id, x, y)
        return getEntity(id)
    }

    fun getEntity(id: UUID): Entity {
        if (!posById.containsKey(id)) {
            throw IllegalArgumentException("Entity ID '$id' does not exist")
        }
        return Entity(id, this)
    }

    fun getPos(id: UUID) =
        posById[id] ?: throw IllegalArgumentException("Entity ID '$id' does not exist")

    fun setPos(id: UUID, x: Float, y: Float) {
        posById[id] = Vector2(x, y)
        locations[id] = Location(x, y)
    }

    operator fun iterator() =
        locations.getSortedIds()
            .map(this::getEntity)
            .iterator()

    inner class Search {
        fun atLocation(location: Location): Set<UUID> =
            locations[location]

        fun <T : Component> withComponent(type: KClass<T>, action: (Entity, T) -> Unit) {
            val matchingEntities = components.getIdsWith(type)
                .map(this@Entities::getEntity)
            for (entity in matchingEntities) {
                val component = entity.getComponent(type) ?: continue
                action.invoke(entity, component)
            }
        }
    }
}
