package bke.iso.v2.engine.world

import bke.iso.v2.app.service.Service
import java.util.*

data class Tile(val texture: String)

@Service
class World {
    private val grid = WorldGrid()

    val entities = Entities(grid)
    val units = Units(tileWidth = 64, tileHeight = 32)

    fun setTile(tile: Tile, x: Int, y: Int) {
        grid.setTile(Location(x, y), tile)
    }

    fun forEach(action: (Location, Tile?, Set<UUID>) -> Unit) =
        grid.forEach(action)
}
