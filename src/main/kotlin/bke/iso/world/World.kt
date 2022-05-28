package bke.iso.world

import bke.iso.Service
import bke.iso.asset.AssetService
import bke.iso.util.getLogger
import com.badlogic.gdx.math.Vector2

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

    val tileWidth: Int = 63
    val tileHeight: Int = 32

    val halfTileHeight: Int
        get() = tileWidth / 2
    val halfTileWidth: Int
        get() = tileHeight / 2

    private val grid = mutableMapOf<Location, GridData>()

    fun getTile(location: Location): Tile? =
        grid[location]?.tile

    fun forEachTile(action: (Location, Tile) -> Unit) {
        grid.forEach { location, data ->
            data.tile?.let { action.invoke(location, it) }
        }
    }

    fun setTile(tile: Tile, location: Location) {
        grid.getOrPut(location) { GridData() }.tile = tile
    }

    fun loadMap(name: String) {
        val map = assetService.getAsset(name, MapData::class)
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

    fun worldToScreen(location: Location): Vector2 =
        worldToScreen(
            // By swapping x and y and then negating them, we're now working  with vectors
            // in an isometric coordinate system where the origin is in the top left corner
            Vector2(
                location.y.toFloat() * -1,
                location.x.toFloat() * -1
            )
        )

    // TODO: test this
    fun worldToScreen(pos: Vector2): Vector2 =
        Vector2(
            (pos.x - pos.y) * (tileWidth / 2).toFloat(),
            (pos.x + pos.y) * (tileHeight / 2).toFloat()
        )

    // TODO: test this
    fun screenToWorld(pos: Vector2): Vector2 =
        Vector2(
            (pos.x / halfTileWidth + pos.y / halfTileWidth) / 2,
            (pos.y / halfTileHeight - (pos.x / halfTileHeight)) / 2
        )

    data class GridData(
        var tile: Tile? = null
    )
}
