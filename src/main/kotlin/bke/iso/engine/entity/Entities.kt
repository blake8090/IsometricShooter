package bke.iso.engine.entity

import bke.iso.app.service.Service
import bke.iso.engine.Location
import bke.iso.engine.log
import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class Component

@Service
class Entities {
    private val dataById = mutableMapOf<UUID, EntityData>()
    private val locationById = mutableMapOf<UUID, Location>()
    private val idsByLocation = mutableMapOf<Location, MutableSet<UUID>>()
        .toSortedMap(
            compareByDescending(Location::y)
                .thenBy(Location::x)
        )
    private val idsByComponentType = mutableMapOf<KClass<out Component>, MutableSet<UUID>>()

    fun create(x: Float = 0f, y: Float = 0f): Entity {
        val id = UUID.randomUUID()
        if (dataById.containsKey(id)) {
            throw IllegalArgumentException("Encountered duplicate entity ID '$id'")
        }

        val data = EntityData()
        data.pos = Vector2(x, y)

        val location = Location(x, y)
        data.location = location
        dataById[id] = data
        updateLocation(id, location)
        return Entity(id, this)
    }

    private fun updateLocation(id: UUID, location: Location) {
        val oldLocation = locationById[id]
        if (oldLocation != null && location != oldLocation) {
            idsByLocation[oldLocation]?.remove(id)
            log.trace("Moving entity $id from $oldLocation to $location")
        }

        idsByLocation.getOrPut(location) { mutableSetOf() }
            .add(id)
        locationById[id] = location
    }

    fun getAll(): List<Entity> =
        idsByLocation
            .flatMap { entry -> entry.value }
            .map { id -> Entity(id, this) }

    fun getPos(id: UUID): Vector2 =
        dataById[id]
            ?.pos
            ?: throw IllegalArgumentException("Entity ID '$id' does not exist")

    fun setPos(id: UUID, pos: Vector2) {
        val data = dataById[id]
            ?: throw IllegalArgumentException("Entity ID '$id' does not exist")
        data.pos = pos
        updateLocation(id, Location(pos))
    }

    fun setPos(id: UUID, x: Float, y: Float) =
        setPos(id, Vector2(x, y))

    fun <T : Component> getComponent(id: UUID, type: KClass<T>): T? {
        val data = dataById[id]
            ?: return null
        val component = data.components[type]
        return type.safeCast(component)
    }

    fun findAllInLocation(location: Location): Set<UUID> =
        idsByLocation[location]
            ?: emptySet()

    fun <T : Component> addComponent(id: UUID, component: T) {
        val data = dataById[id]
            ?: throw IllegalArgumentException("Entity ID '$id' does not exist")
        data.components[component::class] = component
        idsByComponentType
            .getOrPut(component::class) { mutableSetOf() }
            .add(id)
    }

    fun <T : Component> hasComponent(id: UUID, type: KClass<T>): Boolean =
        dataById[id]
            ?.components
            ?.containsKey(type)
            ?: false

    fun <T : Component> removeComponent(id: UUID, type: KClass<T>) {
        val data = dataById[id]
            ?: throw IllegalArgumentException("Entity ID '$id' does not exist")
        data.components.remove(type)
        idsByComponentType[type]?.remove(id)
    }

    fun <T : Component> withComponent(type: KClass<T>, action: (Entity, T) -> Unit) {
        val ids = idsByComponentType[type] ?: return
        for (id in ids) {
            val component = getComponent(id, type) ?: continue
            action.invoke(Entity(id, this), component)
        }
    }

    fun <A : Component, B : Component> withComponents(
        typeA: KClass<A>,
        typeB: KClass<B>,
        action: (UUID, A, B) -> Unit
    ) {
        val setA = idsByComponentType[typeA] ?: return
        val setB = idsByComponentType[typeB] ?: return
        val matchingIds = setA.intersect(setB)

        for (id in matchingIds) {
            val data = dataById[id] ?: return
            val componentA = typeA.safeCast(data.components[typeA]) ?: return
            val componentB = typeB.safeCast(data.components[typeB]) ?: return
            action.invoke(id, componentA, componentB)
        }
    }
}

private class EntityData {
    var pos: Vector2 = Vector2()
    var location: Location = Location(0, 0)
    val components: MutableMap<KClass<out Component>, Component> = mutableMapOf()
}
