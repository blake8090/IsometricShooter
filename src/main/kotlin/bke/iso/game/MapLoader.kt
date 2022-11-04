package bke.iso.game

import bke.iso.engine.FilePointer
import bke.iso.engine.Location
import bke.iso.engine.Tile
import bke.iso.engine.assets.Asset
import bke.iso.engine.assets.AssetLoader
import bke.iso.engine.entity.Sprite
import com.badlogic.gdx.math.Vector2
import kotlin.reflect.KClass

data class MapData(
    val tiles: Map<Location, Tile>,
    val walls: Set<Location>
)

class MapLoader : AssetLoader<MapData>() {
    private val spriteOffset = Vector2(0f, 16f)
    private val floor = Tile(Sprite("floor", spriteOffset))

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
