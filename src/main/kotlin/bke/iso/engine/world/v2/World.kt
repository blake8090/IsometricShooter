package bke.iso.engine.world.v2

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedMap
import com.badlogic.gdx.utils.OrderedSet
import kotlin.math.ceil
import kotlin.math.floor

class World(private val events: Events) : EngineModule() {

    override val moduleName = "world-v2"
    override val updateWhileLoading = false
    override val profilingEnabled = true

    val entities = OrderedSet<Entity>()

    private val entitiesByLocation = OrderedMap<Location, ObjectSet<Entity>>()
    private val locationsByEntity = ObjectMap<Entity, ObjectSet<Location>>()
    private val deletedEntities = mutableSetOf<Entity>()

    private val entityIdLength = 12
    private val entityIdSymbols: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    init {
        // improves performance when removing objects
        entities.orderedItems().ordered = false
    }

    override fun update(deltaTime: Float) {
        for (entity in deletedEntities) {
            delete(entity)
        }
        deletedEntities.clear()
    }

    fun create(location: Location, vararg components: Component): Entity =
        create(
            generateEntityId(),
            location.x.toFloat(),
            location.y.toFloat(),
            location.z.toFloat(),
            *components
        )

    fun create(pos: Vector3, vararg components: Component): Entity =
        create(
            generateEntityId(),
            pos.x,
            pos.y,
            pos.z,
            *components
        )

    fun create(
        id: String,
        x: Float,
        y: Float,
        z: Float,
        vararg components: Component
    ): Entity {
        val entity = Entity(id, onMove = this::update)

        for (component in components) {
            entity.add(component)
        }

        entity.moveTo(x, y, z)
        events.fire(EntityCreated(entity))
        return entity
    }

    private fun generateEntityId() =
        List(entityIdLength) { entityIdSymbols.random() }.joinToString("")

    private fun update(entity: Entity) {
        if (!entities.contains(entity)) {
            entities.add(entity)
        }

        removeLocations(entity)
        val newLocations = getOrPutLocations(entity)
        for (location in entity.getLocations()) {
            // TODO: add some verification here, like that there can't be more than one tile entity in a location
            newLocations.add(location)
        }
    }

    private fun getOrPutLocations(entity: Entity): ObjectSet<Location> {
        if (!locationsByEntity.containsKey(entity)) {
            locationsByEntity.put(entity, ObjectSet())
        }
        return locationsByEntity[entity]
    }

    fun delete(entity: Entity) {
        removeLocations(entity)
        entities.remove(entity)
    }

    private fun removeLocations(entity: Entity) {
        val locations = locationsByEntity.remove(entity) ?: return
        for (location in locations) {
            entitiesByLocation[location]?.remove(entity)
        }
    }

    fun getEntitiesInArea(box: Box): Set<Entity> {
        val minX = floor(box.min.x).toInt()
        val minY = floor(box.min.y).toInt()
        val minZ = floor(box.min.z).toInt()

        val maxX = ceil(box.max.x).toInt()
        val maxY = ceil(box.max.y).toInt()
        val maxZ = ceil(box.max.z).toInt()

        val entities = mutableSetOf<Entity>()
        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    entities.addAll(getEntitiesAt(Location(x, y, z)))
                }
            }
        }
        return entities
    }

    fun getEntitiesAt(location: Location): Set<Entity> =
        entitiesByLocation[location]
            ?.toSet()
            ?: emptySet()

    fun clear() {
        entities.clear()
        entitiesByLocation.clear()
        locationsByEntity.clear()
        deletedEntities.clear()
    }

    data class EntityCreated(val entity: Entity) : Event
}
