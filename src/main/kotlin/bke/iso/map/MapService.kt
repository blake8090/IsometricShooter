package bke.iso.map

import bke.iso.Service
import bke.iso.asset.AssetService
import bke.iso.util.getLogger

data class MapCoord(
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0
)

data class Tile(
    var texture: String,
    var collidable: Boolean = false
) {
    constructor(template: TileTemplate) : this(template.texture, template.collidable)
}

@Service
class MapService(private val assetService: AssetService) {
    private val log = getLogger(this)

    var tileWidth: Int = 64
        private set
    var tileHeight: Int = 32
        private set

    private val tiles = mutableMapOf<MapCoord, Tile>()

    fun loadMap(name: String) {
        val map = assetService.getAsset(name, MapData::class)
        if (map == null) {
            log.warn("Map '$name' was not found")
            return
        }

        val tileTemplates = assetService.getAllAssets(TileTemplate::class)
        val templatesBySymbol = tileTemplates.associateBy { template -> template.symbol }

        tiles.clear()
        map.rows.forEachIndexed { y, symbols ->
            symbols.forEachIndexed { x, symbol ->
                val template = templatesBySymbol[symbol]
                if (template != null) {
                    tiles[MapCoord(x, y)] = Tile(template)
                }
            }
        }

        log.info("Successfully loaded map '$name'")
    }

    fun forEachTile(action: (MapCoord, Tile) -> Unit) =
        tiles.forEach(action)

//    fun setTile(tile: Tile, x: Int, y: Int, z: Int = 0) {
//        tiles[MapCoord(x, y, z)] = tile
//    }
//
//    fun getTile(x: Float, y: Float, z: Float = 0f): Tile? =
//        tiles[
//                MapCoord(
//                    ceil(x / 32f).toInt(),
//                    ceil(y / 32f).toInt(),
//                    ceil(z / 32f).toInt()
//                )
//        ]
//
//    fun getTiles(): List<Pair<MapCoord, Tile>> =
//        tiles.map { entry ->
//            Pair(entry.key, entry.value)
//        }
}
