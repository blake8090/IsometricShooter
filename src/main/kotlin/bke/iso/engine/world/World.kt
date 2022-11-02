package bke.iso.engine.world

import bke.iso.app.service.Service

data class Tile(val sprite: Sprite)

@Service
class World {
    private val grid = WorldGrid()

    val entities = Entities(grid)
    val units = Units(tileWidth = 64, tileHeight = 32)

    fun setTile(tile: Tile, x: Int, y: Int) {
        grid.setTile(Location(x, y), tile)
    }

    fun forEachTile(action: (Location, Tile) -> Unit) =
        grid.forEachTile(action)

    fun forEachEntity(action: (Location, Entity) -> Unit) {
        grid.forEachEntity { location, entityIds ->
            entityIds.forEach { id ->
                action.invoke(location, Entity(id, entities))
            }
        }
    }
}
