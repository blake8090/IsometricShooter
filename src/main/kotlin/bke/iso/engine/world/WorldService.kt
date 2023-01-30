package bke.iso.engine.world

import bke.iso.engine.entity.Entity
import bke.iso.engine.log
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.service.Singleton
import com.badlogic.gdx.math.Rectangle

data class Tile(val sprite: Sprite)

@Singleton
class WorldService {

    private val locationByEntity = mutableMapOf<Entity, Location>()
    private val locations = mutableMapOf<Location, LocationData>()

    fun setTile(location: Location, tile: Tile) {
        locations
            .getOrPut(location) { LocationData() }
            .tile = tile
    }

    fun updateEntity(entity: Entity, x: Float, y: Float, z: Float) {
        val newLocation = Location(x, y, z)
        val oldLocation = locationByEntity[entity]
        if (oldLocation != null && oldLocation != newLocation) {
            removeEntity(entity)
            log.debug("Moving entity ${entity.id} from $oldLocation to $newLocation")
        }
        locationByEntity[entity] = newLocation
        locations
            .getOrPut(newLocation) { LocationData() }
            .entities
            .add(entity)
    }

    fun removeEntity(entity: Entity) {
        val location = locationByEntity[entity] ?: return
        val data = locations[location] ?: return
        data.entities.remove(entity)
        locationByEntity.remove(entity)
    }

    fun getAll(): List<Pair<Location, LocationData>> =
        locations.keys
            .sortedWith(
                compareByDescending(Location::y)
                    .thenBy(Location::x)
            ).mapNotNull { location ->
                locations[location]?.let { data ->
                    location to data
                }
            }

    fun getEntitiesAt(x: Int, y: Int, z: Int): Set<Entity> =
        locations[Location(x, y, z)]
            ?.entities
            ?: emptySet()

    fun findEntitiesInArea(rect: Rectangle): Set<Entity> {
        val startX = rect.x.toInt()
        val endX = (rect.x + rect.width).toInt() + 1

        val startY = rect.y.toInt()
        val endY = (rect.y + rect.height).toInt() + 1

        val entities = mutableSetOf<Entity>()
        for (x in startX..endX) {
            for (y in startY..endY) {
                entities.addAll(getEntitiesAt(x, y, 0))
            }
        }
        return entities
    }
}

data class LocationData(
    var tile: Tile? = null,
    val entities: MutableSet<Entity> = mutableSetOf()
)
