package bke.iso.game

import bke.iso.engine.*
import bke.iso.engine.assets.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.render.RenderService
import bke.iso.engine.render.Sprite
import bke.iso.game.controller.BulletController
import bke.iso.game.controller.PlayerController
import bke.iso.game.controller.TurretController
import bke.iso.game.event.BulletCollisionHandler
import bke.iso.game.event.DamageHandler
import bke.iso.game.event.DrawHealthHandler
import bke.iso.game.event.ShootHandler
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.Keys
import kotlin.system.measureTimeMillis

class GameState(
    private val tiles: Tiles,
    private val assets: Assets,
    private val renderService: RenderService,
    private val input: Input,
    private val entityFactory: EntityFactory
) : State() {
    override val controllers = setOf(
        PlayerController::class,
        TurretController::class,
        BulletController::class
    )

    override val eventHandlers = setOf(
        BulletCollisionHandler::class,
        ShootHandler::class,
        DamageHandler::class,
        DrawHealthHandler::class
    )

    override fun start() {
        renderService.cursor = Sprite("cursor", 16f, 16f)
        buildWorld()
        setupInput()
    }

    private fun setupInput() {
        log.debug("binding actions")
        input.bind("toggleDebug", KeyBinding(Keys.M, InputState.PRESSED))
        input.bind("moveLeft", KeyBinding(Keys.A, InputState.DOWN, true))
        input.bind("moveRight", KeyBinding(Keys.D, InputState.DOWN))
        input.bind("moveUp", KeyBinding(Keys.W, InputState.DOWN))
        input.bind("moveDown", KeyBinding(Keys.S, InputState.DOWN, true))
        input.bind("run", KeyBinding(Keys.SHIFT_LEFT, InputState.DOWN))
        input.bind("shoot", MouseBinding(Buttons.LEFT, InputState.PRESSED))
    }

    private fun buildWorld() {
        log.debug("building world")
        val loadingTime = measureTimeMillis { loadMap() }
        log.debug("built world in $loadingTime ms")
    }

    private fun loadMap() {
        val mapData = assets.get<MapData>("test")
            ?: throw IllegalArgumentException("expected map asset")

        mapData.tiles.forEach { (location, tile) ->
            tiles.setTile(tile, location)
        }

        mapData.walls.forEach { location ->
            entityFactory.createWall(location.x.toFloat(), location.y.toFloat())
        }

        mapData.boxes.forEach { location ->
            entityFactory.createBox(location.x.toFloat(), location.y.toFloat())
        }

        mapData.turrets.forEach { location ->
            entityFactory.createTurret(location.x.toFloat(), location.y.toFloat())
        }

        mapData.players.forEach { location ->
            entityFactory.createPlayer(location.x.toFloat(), location.y.toFloat())
        }
    }
}
