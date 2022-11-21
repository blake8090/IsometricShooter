package bke.iso.engine.entity

import bke.iso.app.service.Service
import bke.iso.engine.Location
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.math.floor
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

    fun inArea(area: Rectangle): List<Entity> {
        val xMin = floor(area.x).toInt()
        val xMax = floor(area.x + area.width).toInt()

        val yMin = floor(area.y).toInt()
        val yMax = floor(area.y + area.height).toInt()

        val idsInArea = mutableSetOf<UUID>()
        for (y in yMin..yMax) {
            for (x in xMin..xMax) {
                val idsAtLocation = locations[Location(x, y)]
                idsInArea.addAll(idsAtLocation)
            }
        }

        return idsInArea.map(this::getEntity)
    }

    fun <T : Component> withComponent(type: KClass<out T>, action: (Entity, T) -> Unit) {
        for (id in components.getIdsWith(type)) {
            val component = components[id, type]
                ?: throw IllegalArgumentException("Expected entity id '$id' to have component '${type.simpleName}'")
            action.invoke(getEntity(id), component)
        }
    }
}
