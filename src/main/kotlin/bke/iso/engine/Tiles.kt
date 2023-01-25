package bke.iso.engine

import bke.iso.service.Singleton
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import java.util.TreeSet

data class Tile(val sprite: Sprite)

private typealias TileMap = TreeSet<TileRecord>

@Singleton
class TileService {

    private val layers = sortedMapOf<Int, TileMap>()

    fun setTile(tile: Tile, location: Location) {
        val tileMap = layers.getOrPut(location.z) { createTileMap() }
        val record = TileRecord(location, tile)
        if (tileMap.contains(record)) {
            tileMap.remove(record)
        }
        tileMap.add(record)
    }

    private fun createTileMap(): TileMap =
        TreeSet<TileRecord>(
            compareByDescending<TileRecord> { record -> record.location.y }
                .thenBy { record -> record.location.x }
        )

    fun forEachTile(action: (Location, Tile) -> Unit) {
        for ((_, layer) in layers) {
            for ((location, tile) in layer) {
                action.invoke(location, tile)
            }
        }
    }
}

private data class TileRecord(
    val location: Location,
    val tile: Tile
)
