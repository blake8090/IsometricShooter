package bke.iso.v2.game

import bke.iso.engine.math.Location
import bke.iso.engine.physics.Velocity
import bke.iso.engine.render.Sprite
import bke.iso.game.asset.GameMap
import bke.iso.game.asset.GameMapLoader
import bke.iso.game.entity.Player
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.GameState
import bke.iso.v2.engine.System
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

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

    override fun update(deltaTime: Float) {
        game.world.actorsWith<Player> { actor, _ ->
            val x =
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                    -1f
                } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                    1f
                } else {
                    0f
                }

            val y =
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                    1f
                } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                    -1f
                } else {
                    0f
                }

            val velocity = actor.components.getOrPut(Velocity())
            velocity.delta.set(x, y, 0f)
            velocity.speed.set(5f, 5f, 5f)

            game.renderer.setCameraPos(actor.pos)
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
