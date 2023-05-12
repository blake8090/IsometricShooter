package bke.iso.game

import bke.iso.engine.log
import bke.iso.engine.asset.AssetService
import bke.iso.engine.event.EventHandler
import bke.iso.engine.input.InputService
import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.render.RenderService
import bke.iso.engine.state.State
import bke.iso.engine.system.System
import bke.iso.engine.world.WorldService
import bke.iso.game.event.BulletCollisionHandler
import bke.iso.game.event.DamageHandler
import bke.iso.game.event.DrawHealthHandler
import bke.iso.game.event.ShootHandler
import bke.iso.game.system.BouncyBallSystem
import bke.iso.game.system.BulletSystem
import bke.iso.game.system.MovingPlatformSystem
import bke.iso.game.system.PlayerSystem
import bke.iso.game.system.TurretSystem
import bke.iso.service.ServiceProvider
import com.badlogic.gdx.Input

class GameState(
    private val assetService: AssetService,
    private val worldService: WorldService,
    private val entityFactory: EntityFactory,
    private val inputService: InputService,
    private val renderService: RenderService,
    private val systemProvider: ServiceProvider<System>,
    private val handlerProvider: ServiceProvider<EventHandler<*>>
) : State() {

    override fun create() {
        addSystems(
            systemProvider,
            PlayerSystem::class,
            BulletSystem::class,
            TurretSystem::class,
            BouncyBallSystem::class,
            MovingPlatformSystem::class
        )

        addHandlers(
            handlerProvider,
            ShootHandler::class,
            DamageHandler::class,
            BulletCollisionHandler::class,
            DrawHealthHandler::class
        )
    }

    override fun start() {
        // TODO: move this to a loading screen
        assetService.loadModule("game")
        renderService.setCursor("cursor")
        bindInput()
        loadMap("collision-test")
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
            worldService.setTile(location, tile)
        }

        mapData.walls.forEach { location ->
            entityFactory.createWall(location)
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

        mapData.platforms.forEach { location ->
            entityFactory.createPlatform(location)
        }
    }
}
