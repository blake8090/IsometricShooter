package bke.iso

import kotlin.math.ceil

data class MapCoord(
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0
)

data class Tile(
    var textureName: String,
    var solid: Boolean = false
)

@Service
class MapService {
    var tileWidth: Int = 64
        private set
    var tileHeight: Int = 128
        private set

    private val tiles = mutableMapOf<MapCoord, Tile>()

    fun setTile(tile: Tile, x: Int, y: Int, z: Int = 0) {
        tiles[MapCoord(x, y, z)] = tile
    }

    fun getTile(x: Float, y: Float, z: Float = 0f): Tile? =
        tiles[
                MapCoord(
                    ceil(x / 32f).toInt(),
                    ceil(y / 32f).toInt(),
                    ceil(z / 32f).toInt()
                )
        ]

    fun getTiles(): List<Pair<MapCoord, Tile>> =
        tiles.map { entry ->
            Pair(entry.key, entry.value)
        }
}
