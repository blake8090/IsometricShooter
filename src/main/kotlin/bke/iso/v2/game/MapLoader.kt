package bke.iso.v2.game

import bke.iso.v2.engine.FilePointer
import bke.iso.v2.engine.assets.Asset
import bke.iso.v2.engine.assets.AssetLoader
import bke.iso.v2.engine.world.Location
import bke.iso.v2.engine.world.Tile
import kotlin.reflect.KClass

data class MapData(
    val tiles: Map<Location, Tile>,
    val walls: Set<Location>
)

class MapLoader : AssetLoader<MapData>() {
    private val floor = Tile("floor")

    override fun getType(): KClass<MapData> =
        MapData::class

    override fun load(file: FilePointer): List<Asset<MapData>> {
        val tiles = mutableMapOf<Location, Tile>()
        val walls = mutableSetOf<Location>()

        val rows = file.readText()
            .lines()
            .map(String::toList)
            .filter { chars -> chars.isNotEmpty() }

        for ((y, row) in rows.withIndex()) {
            for ((x, char) in row.withIndex()) {
                val location = Location(x, y)
                when (fromSymbol(char)) {
                    MapObjects.FLOOR -> tiles[location] = floor
                    MapObjects.WALL -> walls.add(location)
                }
            }
        }

        return listOf(
            Asset(
                file.getNameWithoutExtension(),
                MapData(tiles, walls)
            )
        )
    }

    private fun fromSymbol(char: Char): MapObjects =
        when (char) {
            '.' -> MapObjects.FLOOR
            '#' -> MapObjects.WALL
            else -> MapObjects.FLOOR
        }
}

private enum class MapObjects {
    FLOOR,
    WALL
}
