package bke.iso.v2.game

import bke.iso.v2.engine.math.Location
import bke.iso.v2.engine.math.toScreen
import bke.iso.v2.engine.Event
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.GameState
import bke.iso.v2.engine.input.InputState
import bke.iso.v2.engine.input.KeyBinding
import bke.iso.v2.engine.input.MouseBinding
import bke.iso.v2.engine.render.DrawActorEvent
import bke.iso.v2.engine.render.Sprite
import bke.iso.v2.engine.render.withColor
import bke.iso.v2.engine.world.Actor
import bke.iso.v2.game.actor.BulletSystem
import bke.iso.v2.game.actor.Factory
import bke.iso.v2.game.actor.PlayerSystem
import bke.iso.v2.game.actor.TurretSystem
import bke.iso.v2.game.asset.GameMap
import bke.iso.v2.game.asset.GameMapLoader
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import mu.KotlinLogging

class GameplayState(private val game: Game) : GameState(game) {

    private val log = KotlinLogging.logger {}

    private val factory = Factory(game.world)
    private val combat = Combat(game.world)

    override val systems = setOf(
        PlayerSystem(game.input, game.world, game.renderer, combat),
        TurretSystem(game.world, game.collisions, game.renderer.debugRenderer, combat),
        BulletSystem(game.world, combat)
    )

    override fun start() {
        game.assets.addLoader("map2", GameMapLoader())
        game.assets.load("game")
        game.renderer.setCursor("cursor")

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

    override fun handleEvent(event: Event) {
        when (event) {
            is DrawActorEvent -> drawHealthBar(event.actor, event.batch)
        }
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

    private fun drawHealthBar(actor: Actor, batch: PolygonSpriteBatch) {
        val healthBarWidth = 32f
        val healthBarHeight = 8f

        val health = actor.components[Health::class] ?: return
        val healthBar = actor.components[HealthBar::class] ?: return

        val pixel = game.assets.get<Texture>("pixel")
        val pos = toScreen(actor.pos)
            .sub(healthBar.offsetX, healthBar.offsetY)

        batch.withColor(Color.RED) {
            batch.draw(pixel, pos.x, pos.y, healthBarWidth, healthBarHeight)
        }

        batch.withColor(Color.GREEN) {
            val ratio = health.value / health.maxValue
            val width = healthBarWidth * ratio
            batch.draw(pixel, pos.x, pos.y, width, healthBarHeight)
        }
    }
}
