package bke.iso.v2.game

import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.log
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.game.asset.GameMap
import bke.iso.game.asset.GameMapLoader
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.GameState
import bke.iso.v2.engine.System
import com.badlogic.gdx.Input

class MainGameState(private val game: Game) : GameState(game) {
    override val systems = setOf<System>(
        PlayerSystem(game.input, game.world, game.renderer)
    )

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

        bindInput()
    }

    private fun bindInput() {
        log.debug("binding actions")
        game.input.bind("toggleDebug", KeyBinding(Input.Keys.M, InputState.PRESSED))
        game.input.bind("moveLeft", KeyBinding(Input.Keys.A, InputState.DOWN, true))
        game.input.bind("moveRight", KeyBinding(Input.Keys.D, InputState.DOWN))
        game.input.bind("moveUp", KeyBinding(Input.Keys.W, InputState.DOWN))
        game.input.bind("moveDown", KeyBinding(Input.Keys.S, InputState.DOWN, true))
        game.input.bind("run", KeyBinding(Input.Keys.SHIFT_LEFT, InputState.DOWN))
        game.input.bind("shoot", MouseBinding(Input.Buttons.LEFT, InputState.PRESSED))

        game.input.bind("flyUp", KeyBinding(Input.Keys.E, InputState.DOWN))
        game.input.bind("flyDown", KeyBinding(Input.Keys.Q, InputState.DOWN, true))

        game.input.bind("placeBouncyBall", KeyBinding(Input.Keys.Z, InputState.PRESSED))
        game.input.bind("checkCollisions", KeyBinding(Input.Keys.C, InputState.DOWN))
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
            '1' -> game.world.setTile(location, Sprite("floor", 0f, 16f), true)
            '2' -> game.world.setTile(location, Sprite("floor2", 0f, 16f), true)
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
