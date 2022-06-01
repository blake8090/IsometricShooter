package bke.iso.world

import bke.iso.Service
import bke.iso.asset.AssetService
import bke.iso.util.getLogger
import com.badlogic.gdx.math.Vector2
import kotlin.reflect.KClass

data class Tile(
    var texture: String,
    var collidable: Boolean = false
) {
    constructor(template: TileTemplate) : this(template.texture, template.collidable)
}

/**
 * Represents a location on the world grid.
 */
data class Location(val x: Int, val y: Int)

@Service
class World(private val assetService: AssetService) {
    private val log = getLogger(this)

    val tileWidth: Int = 64
    val halfTileWidth: Int
        get() = tileWidth / 2

    val tileHeight: Int = 32
    val halfTileHeight: Int
        get() = tileHeight / 2

    private val entityDatabase = EntityDatabase()
    private val locationByEntityId = mutableMapOf<Int, Location>()

    // TODO: Make this its own class
    private val grid = mutableMapOf<Location, GridData>()

    fun createEntity(x: Float = 0f, y: Float = 0f): Int? =
        entityDatabase.createEntity()
            ?.apply { setEntityPosition(this, x, y) }

    fun createEntity(components: List<Component>, x: Float = 0f, y: Float = 0f): Int? {
        val id = createEntity(x, y)
        if (id != null) {
            components.forEach { component -> setEntityComponent(id, component) }
        }
        return id
    }

    fun <T : Component> getEntityComponent(id: Int, componentType: KClass<T>): T? =
        entityDatabase.getComponent(id, componentType)

    fun <T : Component> setEntityComponent(id: Int, component: T) =
        entityDatabase.setComponent(id, component)

    fun setEntityPosition(id: Int, x: Float, y: Float) {
        if (!entityDatabase.contains(id)) {
            return
        }

        var positionComponent = entityDatabase.getComponent<PositionComponent>(id)
        if (positionComponent == null) {
            positionComponent = PositionComponent()
            entityDatabase.setComponent(id, positionComponent)
        }
        positionComponent.x = x
        positionComponent.y = y

        //val location = Location((x + .toInt(), (y).toInt())
        val location = Location(x.toInt(), y.toInt())
        val oldLocation = locationByEntityId[id]
        if (oldLocation != null && location != oldLocation) {
            log.debug("removing entity $id from $oldLocation to $location")
            getOrCreateGridData(oldLocation).entities.remove(id)
            locationByEntityId.remove(id)
        }
        getOrCreateGridData(location).entities.add(id)
        locationByEntityId[id] = location
        getOrCreateGridData(location).entities.add(id)
    }

    fun moveEntity(id: Int, dx: Float = 0f, dy: Float = 0f) {
        if (dx == 0f && dy == 0f) {
            return
        }
        val positionComponent = entityDatabase.getComponent<PositionComponent>(id) ?: return
        setEntityPosition(
            id,
            positionComponent.x + dx,
            positionComponent.y + dy
        )
    }

    fun getAllDataByLocation() =
        grid.entries
            .groupBy({ entry -> entry.key.y }, { entry -> Triple(entry.key, entry.value.tile, entry.value.entities) })
            .toSortedMap()

    fun forEachTile(action: (Location, Tile?, Set<Int>) -> Unit) {
        grid.entries
            .sortedBy { entry -> entry.key.y }
            .forEach { (location, data) ->
                action.invoke(location, data.tile, data.entities)
            }
    }

    fun setTile(tile: Tile, location: Location) {
        getOrCreateGridData(location).tile = tile
    }

    private fun getOrCreateGridData(location: Location) =
        grid.getOrPut(location) { GridData() }

    fun loadMap(name: String) {
        val map = assetService.getAsset<MapData>(name)
        if (map == null) {
            log.warn("Map '$name' was not found")
            return
        }

        val templatesBySymbol = assetService.getAllAssets(TileTemplate::class)
            .associateBy { template -> template.symbol }

        grid.clear()

        for ((y, columns) in map.rows.withIndex()) {
            for ((x, symbol) in columns.withIndex()) {
                templatesBySymbol[symbol]
                    ?.let(::Tile)
                    ?.let { tile ->
                        setTile(tile, Location(x, y))
                    }
                    ?: continue
            }
        }
        log.info("Successfully loaded map '$name'")
    }

    fun worldToScreen(location: Location, offset: Vector2 = Vector2()): Vector2 =
        worldToScreen(Vector2(location.x.toFloat(), location.y.toFloat()), offset)

    // TODO: test this
    fun worldToScreen(pos: Vector2, offset: Vector2 = Vector2()): Vector2 {
        // By swapping x and y and then negating them, we're now working  with vectors
        // in an isometric coordinate system where the origin is in the top left corner
        val isoPos = Vector2(pos.y * -1, pos.x * -1)
        val screenPos = Vector2(
            (isoPos.x - isoPos.y) * (tileWidth / 2).toFloat(),
            (isoPos.x + isoPos.y) * (tileHeight / 2).toFloat()
        )
        return screenPos.add(offset)
    }

// TODO: test this
//    fun screenToWorld(pos: Vector2): Vector2 =
//        Vector2(
//            ((pos.x / halfTileWidth + pos.y / halfTileWidth) / 2),
//            (pos.y / halfTileHeight - (pos.x / halfTileHeight)) / 2
//        )

    private data class GridData(
        var tile: Tile? = null,
        val entities: MutableSet<Int> = mutableSetOf()
    )
}
