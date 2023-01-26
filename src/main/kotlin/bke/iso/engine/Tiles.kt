package bke.iso.engine

import bke.iso.service.Singleton
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

data class Tile(val sprite: Sprite)

private typealias TileMap = MutableMap<Location, Tile>

@Singleton
class TileService {

    private val layers = mutableMapOf<Int, TileMap>()

    fun setTile(location: Location, tile: Tile) {
        val layer = layers.getOrPut(location.z) { mutableMapOf() }
        layer[location] = tile
    }

    fun layerCount() =
        layers.filterValues { layer -> layer.isNotEmpty() }
            .keys
            .max()

    fun forEachTileInLayer(z: Int, action: (Location, Tile) -> Unit) {
        val layer = layers[z] ?: return
        for ((location, tile) in layer) {
            action.invoke(location, tile)
        }
    }

    fun forEachTile(action: (Location, Tile) -> Unit) {
        for ((_, layer) in layers) {
            for ((location, tile) in layer) {
                action.invoke(location, tile)
            }
        }
    }
}
