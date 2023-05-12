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
    val players: MutableSet<Location> = mutableSetOf(),
    val platforms: MutableSet<Location> = mutableSetOf(),
    val sideFences: MutableSet<Location> = mutableSetOf(),
    val frontFences: MutableSet<Location> = mutableSetOf()
)

class MapDataLoader : AssetLoader<MapData> {
    private val floorSprite = Sprite("floor", 0f, 16f)

    override fun assetType() = MapData::class

    override fun load(file: FilePointer): Pair<String, MapData> {
        val layers = readLayers(file.readText().lines())
        val mapData = MapData()
        for ((z, layer) in layers.withIndex()) {
            for ((y, row) in layer.rows.reversed().withIndex()) {
                for ((x, char) in row.withIndex()) {
                    loadMapData(mapData, char, Location(x, y, z + layer.zOffset))
                }
            }
        }

        return file.getNameWithoutExtension() to mapData
    }

    private fun readLayers(lines: List<String>): List<Layer> {
        val layers = mutableListOf<Layer>()
        var currentLayer = Layer()

        for (line in lines) {
            if (line.equals("LAYER")) {
                currentLayer = Layer()
            } else if (line.startsWith("Z-OFFSET")) {
                currentLayer.zOffset = line.substringAfter("Z-OFFSET").trim().toInt()
            } else if (line.equals("END")) {
                if (currentLayer.rows.isNotEmpty()) {
                    layers.add(currentLayer)
                }
            } else {
                val chars = line.toList()
                if (chars.isNotEmpty()) {
                    currentLayer.rows.add(chars)
                }
            }
        }

        return layers
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
            '_' -> mapData.platforms.add(location)
            '/' -> mapData.sideFences.add(location)
            '=' -> mapData.frontFences.add(location)
        }
    }
}

private class Layer {
    var zOffset = 0
    val rows = mutableListOf<List<Char>>()
}
