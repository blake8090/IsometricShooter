package bke.iso.game

import bke.iso.service.Transient
import bke.iso.engine.FilePointer
import bke.iso.engine.Tile
import bke.iso.engine.asset.AssetLoader
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

data class MapData(
    val tiles: MutableMap<Location, Tile> = mutableMapOf(),
    val walls: MutableSet<Location> = mutableSetOf(),
    val boxes: MutableSet<Location> = mutableSetOf(),
    val turrets: MutableSet<Location> = mutableSetOf(),
    val players: MutableSet<Location> = mutableSetOf()
)

@Transient
class MapLoader : AssetLoader<MapData> {
    private val floorSprite = Sprite("floor", 0f, 16f)

    override fun assetType() = MapData::class

    override fun load(file: FilePointer): Pair<String, MapData> {
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

        return file.getNameWithoutExtension() to mapData
    }

    private fun loadMapData(mapData: MapData, char: Char, location: Location) {
        mapData.tiles[location] = Tile(floorSprite)
        when (char) {
            '#' -> mapData.walls.add(location)
            'x' -> mapData.boxes.add(location)
            't' -> mapData.turrets.add(location)
            'p' -> mapData.players.add(location)
        }
    }
}
