package bke.iso.engine.world

import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.service.SingletonService
import com.badlogic.gdx.math.Rectangle
import java.util.UUID

class WorldService : SingletonService {

    private val deletedObjects = mutableSetOf<WorldObject>()
    private val locationByObject = mutableMapOf<WorldObject, Location>()
    private val grid = mutableMapOf<Location, MutableSet<WorldObject>>()

    val entities = Entities(grid)

    fun setTile(location: Location, sprite: Sprite) {
        val tile = Tile(sprite)
        setup(tile, location.x.toFloat(), location.y.toFloat(), location.z.toFloat())
    }

    fun createEntity(x: Float, y: Float, z: Float): Entity {
        val entity = Entity(UUID.randomUUID())
        setup(entity, x, y, z)
        return entity
    }

    fun createEntity(location: Location) =
        createEntity(location.x.toFloat(), location.y.toFloat(), location.z.toFloat())

    private fun setup(worldObject: WorldObject, x: Float, y: Float, z: Float) {
        worldObject.x = x
        worldObject.y = y
        worldObject.z = z
        add(worldObject, x, y, z)
        worldObject.positionListeners.add { obj ->
            update(obj, obj.x, obj.y, obj.z)
        }
    }

    fun getAllObjects(): List<WorldObject> =
        grid.flatMap { (_, objects) -> objects }

    fun findEntitiesInArea(rect: Rectangle): Set<Entity> {
        val startX = rect.x.toInt()
        val endX = (rect.x + rect.width).toInt() + 1
        val startY = rect.y.toInt()
        val endY = (rect.y + rect.height).toInt() + 1

        val entities = mutableSetOf<Entity>()
        for (x in startX..endX) {
            for (y in startY..endY) {
                getObjectsAt(x, y, 0)
                    .filterIsInstance<Entity>()
                    .forEach(entities::add)
            }
        }
        return entities
    }

    fun getObjectsAt(x: Int, y: Int, z: Int): Set<WorldObject> =
        grid[Location(x, y, z)]
            ?: emptySet()

    fun delete(worldObject: WorldObject) {
        deletedObjects.add(worldObject)
    }

    fun update() {
        // TODO: add delete function for objects?
        deletedObjects.forEach(this::remove)
        deletedObjects.clear()
    }

    private fun update(worldObject: WorldObject, x: Float, y: Float, z: Float) {
        val newLocation = Location(x, y, z)
        val oldLocation = locationByObject[worldObject]
        if (oldLocation != null && oldLocation != newLocation) {
            remove(worldObject)
        }
        add(worldObject, x, y, z)
    }

    private fun remove(worldObject: WorldObject) {
        val location = locationByObject[worldObject] ?: return
        grid[location]?.remove(worldObject)
    }

    private fun add(worldObject: WorldObject, x: Float, y: Float, z: Float) {
        val location = Location(x, y, z)
        grid.getOrPut(location) { mutableSetOf() }.add(worldObject)
        locationByObject[worldObject] = location
    }
}
