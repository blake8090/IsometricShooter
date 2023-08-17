package bke.iso.game

import bke.iso.engine.math.Location
import bke.iso.engine.math.toScreen
import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.GameState
import bke.iso.engine.System
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.ControllerAxisBinding
import bke.iso.engine.input.ControllerBinding
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.render.DrawActorEvent
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.withColor
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.world.Actor
import bke.iso.game.actor.BulletSystem
import bke.iso.game.actor.Factory
import bke.iso.game.actor.PLAYER_MAX_HEALTH
import bke.iso.game.actor.Player
import bke.iso.game.actor.PlayerSystem
import bke.iso.game.actor.TurretSystem
import bke.iso.game.actor.createPlayer
import bke.iso.game.asset.GameMap
import bke.iso.game.asset.GameMapLoader
import bke.iso.game.ui.CrosshairCursor
import bke.iso.game.ui.GameHUD
import bke.iso.game.ui.LoadingScreen
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton
import mu.KotlinLogging

class GameplayState(override val game: Game) : GameState() {

    private val log = KotlinLogging.logger {}

    private val factory = Factory(game.world)
    private val combat = Combat(game.world, game.events)
    private val crosshair = CrosshairCursor(game.assets, game.input)

    override val systems: Set<System> = setOf(
        PlayerSystem(game.input, game.world, game.renderer, combat),
        TurretSystem(game.world, game.collisions, game.renderer.debugRenderer, combat),
        BulletSystem(game.world, combat)
    )

    override var loadingScreen: UIScreen? = LoadingScreen(game.assets)

    override fun load() {
        game.assets.addLoader("map2", GameMapLoader())
        game.assets.load("game")

        loadMap()
        factory.createLampPost(Location(4, 4, 0)).y -= 0.125f
        factory.createLampPost(Location(8, 4, 0)).y -= 0.125f
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

    override fun start() {
        game.renderer.setCursor(crosshair)
        game.ui.setScreen(GameHUD(game.assets))
        game.events.fire(GameHUD.UpdateEvent(PLAYER_MAX_HEALTH))
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is DrawActorEvent -> drawHealthBar(event.actor, event.batch)

            is OnDamagePlayerEvent -> game.events.fire(
                GameHUD.UpdateEvent(
                    PLAYER_MAX_HEALTH,
                    event.health
                )
            )
        }
    }

    private fun bindInput() {
        log.debug { "binding actions" }
        with(game.input.keyMouse) {
            // keyboard & mouse
            bind(
                "toggleDebug" to KeyBinding(Input.Keys.M, ButtonState.PRESSED),
                "placeBouncyBall" to KeyBinding(Input.Keys.Z, ButtonState.PRESSED),
                "run" to KeyBinding(Input.Keys.SHIFT_LEFT, ButtonState.DOWN),
                "shoot" to MouseBinding(Input.Buttons.LEFT, ButtonState.PRESSED),
            )
            bind(
                "moveY",
                KeyBinding(Input.Keys.S, ButtonState.DOWN),
                KeyBinding(Input.Keys.W, ButtonState.DOWN)
            )
            bind(
                "moveX",
                KeyBinding(Input.Keys.A, ButtonState.DOWN),
                KeyBinding(Input.Keys.D, ButtonState.DOWN)
            )
            bind(
                "fly",
                KeyBinding(Input.Keys.Q, ButtonState.DOWN),
                KeyBinding(Input.Keys.E, ButtonState.DOWN)
            )
        }

        with(game.input.controller) {
            // controller
            bind(
                "run" to ControllerBinding(ControllerButton.LEFTBUMPER.ordinal, ButtonState.DOWN),
                "moveX" to ControllerAxisBinding(ControllerAxis.LEFTX.ordinal),
                "moveY" to ControllerAxisBinding(ControllerAxis.LEFTY.ordinal, true),
                "cursorX" to ControllerAxisBinding(ControllerAxis.RIGHTX.ordinal),
                "cursorY" to ControllerAxisBinding(ControllerAxis.RIGHTY.ordinal),
                "shoot" to ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.PRESSED)
            )
            bind(
                "fly",
                ControllerBinding(ControllerButton.X.ordinal, ButtonState.DOWN),
                ControllerBinding(ControllerButton.A.ordinal, ButtonState.DOWN)
            )
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
            'p' -> game.world.createPlayer(location)
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
        // we already display the player's health in the HUD!
        if (actor.has<Player>()) {
            return
        }

        val healthBarWidth = 32f
        val healthBarHeight = 8f

        val health = actor.get<Health>() ?: return
        val healthBar = actor.get<HealthBar>() ?: return

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
