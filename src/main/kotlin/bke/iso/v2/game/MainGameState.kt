package bke.iso.v2.game

import bke.iso.engine.math.Location
import bke.iso.game.asset.GameMap
import bke.iso.game.asset.GameMapLoader
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.GameState
import bke.iso.v2.engine.System

class MainGameState(private val game: Game) : GameState(game) {
    override val systems = emptySet<System>()

    private val factory = Factory(game.world)

    override fun start() {
        game.assets.addLoader("map2", GameMapLoader())
        game.assets.load("game")
        loadMap()

        factory.createLampPost(Location(4, 4, 0))
        factory.createLampPost(Location(8, 4, 0))

        factory.createPillar(Location(12, 12, 0))
            .apply {
                x -= 0.5f
                y += 0.5f
            }
        factory.createPillar(Location(10, 12, 0))
            .apply {
                x -= 0.5f
                y += 0.5f
            }
    }

    private fun loadMap() {
        val gameMap = game.assets.get<GameMap>("collision-test")
        for (layer in gameMap.layers) {
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
    }

    private fun readTile(char: Char, location: Location) {
        when (char) {
            '1' -> game.world.setTile(location, "floor", true)
            '2' -> game.world.setTile(location, "floor2", true)
        }
    }

    private fun readEntity(char: Char, location: Location) {
        when (char) {
            'p' -> factory.createPlayer(location)
            '#' -> factory.createWall(location)
            'x' -> factory.createBox(location)
            't' -> factory.createTurret(location)
            '_' -> factory.createPlatform(location)
            '/' -> factory.createSideFence(location)
            '=' -> factory.createFrontFence(location)
            '|' -> factory.createPillar(location)
        }
    }
}
