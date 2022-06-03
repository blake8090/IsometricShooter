package bke.iso.world

import bke.iso.Singleton
import bke.iso.asset.AssetService
import bke.iso.util.getLogger
import bke.iso.world.asset.MapData
import bke.iso.world.asset.TileTemplate
import bke.iso.world.entity.Component
import bke.iso.world.entity.Entity
import bke.iso.world.entity.EntityDatabase
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

@Singleton
class World(private val assetService: AssetService) {
    private val log = getLogger(this)

    val tileWidth: Int = 64
    val halfTileWidth: Int
        get() = tileWidth / 2

    val tileHeight: Int = 32
    val halfTileHeight: Int
        get() = tileHeight / 2

    private val entityDatabase = EntityDatabase()
    private val worldGrid = WorldGrid()

    fun getTile(location: Location) =
        worldGrid.getTile(location)

    fun setTile(tile: Tile, location: Location) =
        worldGrid.setTile(tile, location)

    fun createEntity() =
        entityDatabase.createEntity()
            ?.let(this::getEntity)

    fun createEntity(vararg components: Component, pos: Vector3 = Vector3()): Entity? {
        val entity = createEntity() ?: return null
        components.forEach(entity::setComponent)
        entity.setPosition(pos)
        return entity
    }

    fun getEntity(id: Int) =
        if (!entityDatabase.contains(id)) {
            null
        } else {
            Entity(
                id,
                entityDatabase,
                worldGrid
            )
        }

    fun forEachLocation(action: (Location, Tile?, Set<Entity>) -> Unit) {
        worldGrid.getAll()
            .forEach { (location, data) ->
                action.invoke(
                    location,
                    data.tile,
                    data.entities
                        .map(this::getEntity)
                        .filterNotNull()
                        .toSet()
                )
            }
    }

    fun loadMap(name: String) {
        val map = assetService.getAsset<MapData>(name)
        if (map == null) {
            log.warn("Map '$name' was not found")
            return
        }

        val templatesBySymbol = assetService.getAllAssets(TileTemplate::class)
            .associateBy { template -> template.symbol }

        worldGrid.clear()

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

    // TODO: move these to another class
    fun locationToScreenPos(location: Location, offset: Vector2 = Vector2()): Vector2 {
        val worldPos = Vector3(
            location.x.toFloat(),
            location.y.toFloat(),
            0f
        )
        val screenPos = toScreen(worldPos)
        screenPos.add(offset)
        screenPos.x -= halfTileWidth
        screenPos.y -= tileHeight
        return screenPos
    }

    fun getEntityScreenPos(entityPos: Vector3, offset: Vector2 = Vector2()): Vector2 {
        val screenPos = toScreen(entityPos)
        screenPos.add(offset)
        return screenPos
    }

    private fun toScreen(worldPos: Vector3): Vector2 {
        // TODO: reword this comment
        // By swapping x and y and then negating them, we're now working  with vectors
        // in an isometric coordinate system where the origin is in the top left corner.
        val screenPos = Vector2(
            (worldPos.y - worldPos.x) * -1,
            (worldPos.y + worldPos.x) * -1,
        )
        screenPos.x *= halfTileWidth
        screenPos.y *= halfTileHeight
        return screenPos
    }
}
