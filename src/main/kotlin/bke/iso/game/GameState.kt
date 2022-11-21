package bke.iso.game

import bke.iso.engine.*
import bke.iso.engine.assets.Assets
import bke.iso.engine.entity.Sprite
import bke.iso.engine.input.Input
import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.system.*
import bke.iso.game.system.PlayerCameraSystem
import bke.iso.game.system.PlayerInputSystem
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

class GameState(
    private val tiles: Tiles,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input,
    private val entityFactory: EntityFactory
) : State() {

    override fun start() {
        renderer.mouseCursor = Sprite("cursor", Vector2(16f, 16f))

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

    override fun getSystems(): List<KClass<out System>> =
        listOf(
            PlayerInputSystem::class,
            CollisionSystem::class,
            PhysicsSystem::class,
            PlayerCameraSystem::class
        )

    private fun buildWorld() {
        log.debug("building world")
        val loadingTime = measureTimeMillis {
            loadMap()
            val player = entityFactory.createPlayer()
            log.debug("Player id: ${player.id}")
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
            entityFactory.createWall(location.x.toFloat(), location.y.toFloat())
        }
    }
}
