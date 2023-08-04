package bke.iso.old.game.asset

import bke.iso.old.engine.math.Location
import bke.iso.old.engine.render.Sprite
import bke.iso.old.engine.world.WorldService
import bke.iso.old.game.entity.EntityFactory
import bke.iso.old.service.SingletonService

class GameMapService(
    private val worldService: WorldService,
    private val entityFactory: EntityFactory
) : SingletonService {

    private val floorSprite = Sprite("floor", 0f, 16f)
    private val floor2Sprite = Sprite("floor2", 0f, 16f)

    fun load(gameMap: GameMap) {
        for (layer in gameMap.layers) {
            loadLayer(layer)
        }
    }

    private fun loadLayer(layer: GameMap.Layer) {
        for ((y, row) in layer.tiles.reversed().withIndex()) {
            for ((x, char) in row.withIndex()) {
                readTile(char, Location(x, y, layer.z))
            }
        }

        for ((y, row) in layer.entities.reversed().withIndex()) {
            for ((x, char) in row.withIndex()) {
                readEntity(char, Location(x, y, layer.z))
            }
        }
    }

    private fun readTile(char: Char, location: Location) {
        when (char) {
            '1' -> worldService.setTile(location, floorSprite, true)
            '2' -> worldService.setTile(location, floor2Sprite, true)
        }
    }

    private fun readEntity(char: Char, location: Location) {
        when (char) {
            'p' -> entityFactory.createPlayer(location)
            '#' -> entityFactory.createWall(location)
            'x' -> entityFactory.createBox(location)
            't' -> entityFactory.createTurret(location)
            '_' -> entityFactory.createPlatform(location)
            '/' -> entityFactory.createSideFence(location)
            '=' -> entityFactory.createFrontFence(location)
            '|' -> entityFactory.createPillar(location)
        }
    }
}