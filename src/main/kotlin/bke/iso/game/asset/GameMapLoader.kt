package bke.iso.game.asset

import bke.iso.engine.FilePointer
import bke.iso.engine.asset.AssetLoader

class GameMapLoader : AssetLoader<GameMap> {
    override fun assetType() = GameMap::class

    override fun load(file: FilePointer): Pair<String, GameMap> {
        var mode = Mode.NONE
        val layers = mutableListOf<GameMap.Layer>()

        var currentLayer = GameMap.Layer(0)
        layers.add(currentLayer)

        val lines = file.readText().lines()
            .filter(String::isNotBlank)
        for (line in lines) {
            if (line.startsWith("LAYER")) {
                val z = line.substringAfter("LAYER").trim().toInt()
                currentLayer = GameMap.Layer(z)
                layers.add(currentLayer)
            } else if (line.equals("TILES")) {
                mode = Mode.TILES
            } else if (line.equals("OBJECTS")) {
                mode = Mode.ENTITIES
            } else {
                when (mode) {
                    Mode.TILES -> currentLayer.tiles.add(line)
                    Mode.ENTITIES -> currentLayer.entities.add(line)
                    else -> throw IllegalArgumentException("no mode")
                }
            }
        }

        return file.getNameWithoutExtension() to GameMap(layers)
    }

    private enum class Mode {
        TILES,
        ENTITIES,
        NONE
    }
}
