package bke.iso.game

import bke.iso.engine.log
import bke.iso.service.Provider
import bke.iso.service.Transient
import bke.iso.engine.TileService
import bke.iso.engine.asset.AssetService
import bke.iso.engine.event.EventHandler
import bke.iso.engine.input.InputService
import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.physics.Collision
import bke.iso.engine.render.RenderService
import bke.iso.engine.state.State
import bke.iso.engine.system.System
import bke.iso.game.event.BulletCollisionHandler
import bke.iso.game.event.DamageHandler
import bke.iso.game.event.DrawHealthHandler
import bke.iso.game.event.ShootHandler
import bke.iso.game.system.BouncyBallSystem
import bke.iso.game.system.BulletSystem
import bke.iso.game.system.PlayerSystem
import bke.iso.game.system.TurretSystem
import bke.iso.service.PostInit
import com.badlogic.gdx.Input

@Transient
class GameState(
    private val assetService: AssetService,
    private val tileService: TileService,
    private val entityFactory: EntityFactory,
    private val inputService: InputService,
    private val renderService: RenderService,
    private val systemProvider: Provider<System>,
    private val handlerProvider: Provider<EventHandler<*>>
) : State() {

    @PostInit
    fun setup() {
        // TODO: add helper methods in State for adding systems and handlers
        systems.add(systemProvider.get(PlayerSystem::class))
        systems.add(systemProvider.get(BulletSystem::class))
        systems.add(systemProvider.get(TurretSystem::class))
        systems.add(systemProvider.get(BouncyBallSystem::class))

        eventHandlers.add(handlerProvider.get(ShootHandler::class))
        eventHandlers.add(handlerProvider.get(DamageHandler::class))
        eventHandlers.add(handlerProvider.get(BulletCollisionHandler::class))
        eventHandlers.add(handlerProvider.get(DrawHealthHandler::class))
    }

    override fun start() {
        // TODO: move this to a loading screen
        assetService.loadModule("game")
        renderService.setCursor("cursor")
        bindInput()
        loadMap("zlevel_test")
    }

    private fun bindInput() {
        log.debug("binding actions")
        inputService.bind("toggleDebug", KeyBinding(Input.Keys.M, InputState.PRESSED))
        inputService.bind("moveLeft", KeyBinding(Input.Keys.A, InputState.DOWN, true))
        inputService.bind("moveRight", KeyBinding(Input.Keys.D, InputState.DOWN))
        inputService.bind("moveUp", KeyBinding(Input.Keys.W, InputState.DOWN))
        inputService.bind("moveDown", KeyBinding(Input.Keys.S, InputState.DOWN, true))
        inputService.bind("run", KeyBinding(Input.Keys.SHIFT_LEFT, InputState.DOWN))
        inputService.bind("shoot", MouseBinding(Input.Buttons.LEFT, InputState.PRESSED))

        inputService.bind("flyUp", KeyBinding(Input.Keys.E, InputState.DOWN))
        inputService.bind("flyDown", KeyBinding(Input.Keys.Q, InputState.DOWN, true))

        inputService.bind("placeBouncyBall", KeyBinding(Input.Keys.Z, InputState.PRESSED))
    }

    private fun loadMap(mapName: String) {
        val mapData = assetService.get<MapData>(mapName)
            ?: throw IllegalArgumentException("expected map asset '$mapName'")

        mapData.tiles.forEach { (location, tile) ->
            tileService.setTile(location, tile)
        }

        mapData.walls.forEach { location ->
            val wall = entityFactory.createWall(location)
            if (location.z > 0) {
                wall.remove<Collision>()
            }
        }

        mapData.boxes.forEach { location ->
            entityFactory.createBox(location)
        }

        mapData.turrets.forEach { location ->
            entityFactory.createTurret(location)
        }

        mapData.players.forEach { location ->
            entityFactory.createPlayer(location)
        }
    }
}
