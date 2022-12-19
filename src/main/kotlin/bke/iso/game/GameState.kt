package bke.iso.game

import bke.iso.engine.*
import bke.iso.engine.assets.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.entity.EntityService
import bke.iso.engine.physics.Bounds
import bke.iso.engine.physics.Collision
import bke.iso.game.controller.PlayerController
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.Keys
import kotlin.system.measureTimeMillis

class GameState(
    private val tiles: Tiles,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input,
    private val entityService: EntityService,
) : State() {
    override val controllers = setOf(PlayerController::class)

    override val eventHandlers = setOf(
        BulletCollisionHandler::class,
        ShootHandler::class
    )

    override fun start() {
        renderer.cursor = Sprite("cursor", 16f, 16f)

        buildWorld()

        log.debug("binding actions")
        input.bind("toggleDebug", KeyBinding(Keys.M, InputState.PRESSED))
        input.bind("moveLeft", KeyBinding(Keys.LEFT, InputState.DOWN, true))
        input.bind("moveRight", KeyBinding(Keys.RIGHT, InputState.DOWN))
        input.bind("moveUp", KeyBinding(Keys.UP, InputState.DOWN))
        input.bind("moveDown", KeyBinding(Keys.DOWN, InputState.DOWN, true))
        input.bind("run", KeyBinding(Keys.SHIFT_LEFT, InputState.DOWN))
        input.bind("shoot", MouseBinding(Buttons.LEFT, InputState.PRESSED))
    }

    private fun buildWorld() {
        log.debug("building world")
        val loadingTime = measureTimeMillis {
            loadMap()
            createPlayer()
        }
        log.debug("built world in $loadingTime ms")
    }

    private fun loadMap() {
        val mapData = assets.get<MapData>("test")
            ?: throw IllegalArgumentException("expected map asset")

        mapData.tiles.forEach { (location, tile) ->
            tiles.setTile(tile, location)
        }

        mapData.walls.forEach { location ->
            val wall = entityService.create(location)
            wall.add(
                Sprite("wall3", 0f, 16f),
                Collision(
                    Bounds(1f, 1f, 0f, 0f),
                    true
                )
            )
        }

        createTurret(7.5f, 13.5f)
    }

    private fun createPlayer() {
        val player = entityService.create()
        player.add(
            Sprite("player", 32f, 0f),
            Player(),
            Collision(
                Bounds(0.5f, 0.5f, -0.25f, -0.25f),
                false
            )
        )
        log.debug("Player id: ${player.id}")
    }

    private fun createTurret(x: Float, y: Float) {
        val turret = entityService.create(x, y)
        turret.add(
            Sprite("turret", 16f, 0f),
            Collision(
                Bounds(0.5f, 0.5f, -0.25f, -0.25f),
                false
            )
        )
    }
}
