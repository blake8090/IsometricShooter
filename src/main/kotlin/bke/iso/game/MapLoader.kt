package bke.iso.game

import bke.iso.service.Transient
import bke.iso.engine.FilePointer
import bke.iso.engine.Tile
import bke.iso.engine.asset.Asset
import bke.iso.engine.asset.AssetLoader
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import kotlin.reflect.KClass

data class MapData(
    val tiles: MutableMap<Location, Tile> = mutableMapOf(),
    val walls: MutableSet<Location> = mutableSetOf(),
    val boxes: MutableSet<Location> = mutableSetOf(),
    val turrets: MutableSet<Location> = mutableSetOf(),
    val players: MutableSet<Location> = mutableSetOf()
)

@Transient
class MapLoader : AssetLoader<MapData>() {
    private val floorSprite = Sprite("floor", 0f, 16f)

    override fun getType(): KClass<MapData> =
        MapData::class

    override fun load(file: FilePointer): List<Asset<MapData>> {
        val rows = file.readText()
            .lines()
            .map(String::toList)
            .filter { chars -> chars.isNotEmpty() }
            .reversed()

        val mapData = MapData()
        for ((y, row) in rows.withIndex()) {
            for ((x, char) in row.withIndex()) {
                loadMapData(mapData, char, Location(x, y))
            }
        }

        return listOf(Asset(file.getNameWithoutExtension(), mapData))
    }

    private fun loadMapData(mapData: MapData, char: Char, location: Location) {
        when (fromSymbol(char)) {
            MapObjects.FLOOR -> mapData.tiles[location] = Tile(floorSprite)
            MapObjects.WALL -> mapData.walls.add(location)

            MapObjects.BOX -> {
                mapData.tiles[location] = Tile(floorSprite)
                mapData.boxes.add(location)
            }

            MapObjects.TURRET -> {
                mapData.tiles[location] = Tile(floorSprite)
                mapData.turrets.add(location)
            }

            MapObjects.PLAYER -> {
                mapData.tiles[location] = Tile(floorSprite)
                mapData.players.add(location)
            }
        }
    }

    private fun fromSymbol(char: Char): MapObjects =
        when (char) {
            '.' -> MapObjects.FLOOR
            '#' -> MapObjects.WALL
            'x' -> MapObjects.BOX
            't' -> MapObjects.TURRET
            'p' -> MapObjects.PLAYER
            else -> MapObjects.FLOOR
        }
}

private enum class MapObjects {
    FLOOR,
    WALL,
    BOX,
    TURRET,
    PLAYER
}
