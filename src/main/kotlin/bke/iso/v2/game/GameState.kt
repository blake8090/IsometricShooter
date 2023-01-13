package bke.iso.v2.game

import bke.iso.engine.log
import bke.iso.service.Provider
import bke.iso.service.Transient
import bke.iso.v2.engine.TileService
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.event.EventHandler
import bke.iso.v2.engine.input.InputService
import bke.iso.v2.engine.input.InputState
import bke.iso.v2.engine.input.KeyBinding
import bke.iso.v2.engine.input.MouseBinding
import bke.iso.v2.engine.render.RenderService
import bke.iso.v2.engine.state.State
import bke.iso.v2.engine.system.System
import bke.iso.v2.game.event.BulletCollisionHandler
import bke.iso.v2.game.event.DamageHandler
import bke.iso.v2.game.event.DrawHealthHandler
import bke.iso.v2.game.event.ShootHandler
import bke.iso.v2.game.system.BulletSystem
import bke.iso.v2.game.system.PlayerSystem
import bke.iso.v2.game.system.TurretSystem
import com.badlogic.gdx.Input

@Transient
class GameState(
    private val assetService: AssetService,
    private val tileService: TileService,
    private val entityFactory: EntityFactory,
    private val inputService: InputService,
    private val renderService: RenderService,
    systemProvider: Provider<System>,
    private val handlerProvider: Provider<EventHandler<*>>
) : State() {
    // TODO: don't use override val, just have a protected val to avoid initialization issues
    override val systems = setOf(
        systemProvider.get(PlayerSystem::class),
        systemProvider.get(BulletSystem::class),
        systemProvider.get(TurretSystem::class)
    )

    override fun start() {
        log.debug("on start")

        eventHandlers.add(handlerProvider.get(ShootHandler::class))
        eventHandlers.add(handlerProvider.get(DamageHandler::class))
        eventHandlers.add(handlerProvider.get(BulletCollisionHandler::class))
        eventHandlers.add(handlerProvider.get(DrawHealthHandler::class))

        assetService.load("assets")
        renderService.setCursor("cursor")

        bindInput()
        loadMap()
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
    }

    private fun loadMap() {
        val mapData = assetService.get<MapData>("test")
            ?: throw IllegalArgumentException("expected map asset")

        mapData.tiles.forEach { (location, tile) ->
            tileService.setTile(tile, location)
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
