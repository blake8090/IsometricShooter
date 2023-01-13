package bke.iso.engine

import bke.iso.service.Singleton
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

data class Tile(val sprite: Sprite)

@Singleton
class TileService {
    private val tileByLocation = mutableMapOf<Location, Tile>()
        .toSortedMap(
            compareByDescending(Location::y)
                .thenBy(Location::x)
        )

    fun setTile(tile: Tile, location: Location) {
        tileByLocation[location] = tile
    }

    fun forEachTile(action: (Location, Tile) -> Unit) {
        tileByLocation.forEach { (location, tile) ->
            action.invoke(location, tile)
        }
    }
}
