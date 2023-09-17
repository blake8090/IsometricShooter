package bke.iso.engine.render3d

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.game.actor.Factory
import bke.iso.game.actor.createMovingPlatform
import bke.iso.game.asset.GameMap
import bke.iso.game.asset.GameMapCache
import bke.iso.game.createShadow
import bke.iso.game.player.createPlayer
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector3

private const val CAMERA_MOVE_SPEED = 3f

class Test3dState(override val game: Game) : State() {
    override val systems: Set<System> = emptySet()

    private val factory = Factory(game.world)

    override suspend fun load() {
        game.assets.register(GameMapCache())
        game.assets.loadAsync("game")
        loadMap()

        game.input.keyMouse.bind(
            "moveX",
            KeyBinding(Input.Keys.A, ButtonState.DOWN),
            KeyBinding(Input.Keys.D, ButtonState.DOWN)
        )

        game.input.keyMouse.bind(
            "moveY",
            KeyBinding(Input.Keys.S, ButtonState.DOWN),
            KeyBinding(Input.Keys.W, ButtonState.DOWN)
        )
    }

    override fun update(deltaTime: Float) {
        val delta = Vector3(
            game.input.poll("moveX") * CAMERA_MOVE_SPEED,
            game.input.poll("moveY") * CAMERA_MOVE_SPEED,
            0f
        )
        game.renderer3D.moveCamera(delta.scl(deltaTime))
    }

    private fun loadMap() {
        val gameMap = game.assets.get<GameMap>("collision-test.map2")
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
            '1' -> game.world.setTile(location, Sprite("floor-flat.png", 0f, 16f))
            '2' -> game.world.setTile(location, Sprite("floor-flat2.png", 0f, 16f))
        }
    }

    private fun readEntity(char: Char, location: Location) {
        when (char) {
            'p' -> {
                val player = game.world.createPlayer(location)
                game.world.createShadow(player)
            }

            '#' -> factory.createWall(location)
            'x' -> factory.createBox(location)
            't' -> factory.createTurret(location)
            '_' -> game.world.createMovingPlatform(location)
            '/' -> factory.createSideFence(location)
            '=' -> factory.createFrontFence(location)
            '|' -> factory.createPillar(location)
        }
    }
}