package bke.iso.game

import bke.iso.engine.FilePointer
import bke.iso.engine.asset.AssetLoader
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite

data class MapData(
    val tiles: MutableMap<Location, Sprite> = mutableMapOf(),
    val walls: MutableSet<Location> = mutableSetOf(),
    val boxes: MutableSet<Location> = mutableSetOf(),
    val turrets: MutableSet<Location> = mutableSetOf(),
    val players: MutableSet<Location> = mutableSetOf()
)

class MapDataLoader : AssetLoader<MapData> {
    private val floorSprite = Sprite("floor", 0f, 16f)

    override fun assetType() = MapData::class

    override fun load(file: FilePointer): Pair<String, MapData> {
        val layers = mutableListOf<Layer>()
        var currentLayer = Layer()
        for (line in file.readText().lines()) {
            when (line) {
                "LAYER" -> currentLayer = Layer()

                "END" -> {
                    if (currentLayer.rows.isNotEmpty()) {
                        layers.add(currentLayer)
                    }
                }

                else -> {
                    val chars = line.toList()
                    if (chars.isNotEmpty()) {
                        currentLayer.rows.add(chars)
                    }
                }
            }
        }

        val mapData = MapData()
        for ((z, layer) in layers.withIndex()) {
            for ((y, row) in layer.rows.reversed().withIndex()) {
                for ((x, char) in row.withIndex()) {
                    loadMapData(mapData, char, Location(x, y, z))
                }
            }
        }

        return file.getNameWithoutExtension() to mapData
    }

    private fun loadMapData(mapData: MapData, char: Char, location: Location) {
        if (char == ',') {
            return
        }
        mapData.tiles[location] = floorSprite
        when (char) {
            '#' -> mapData.walls.add(location)
            'x' -> mapData.boxes.add(location)
            't' -> mapData.turrets.add(location)
            'p' -> mapData.players.add(location)
        }
    }
}

private class Layer {
    val rows = mutableListOf<List<Char>>()
}
