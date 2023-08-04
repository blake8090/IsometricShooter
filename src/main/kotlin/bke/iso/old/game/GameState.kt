package bke.iso.old.game

import bke.iso.old.engine.log
import bke.iso.old.engine.asset.AssetService
import bke.iso.old.engine.event.EventHandler
import bke.iso.old.engine.input.InputService
import bke.iso.old.engine.input.InputState
import bke.iso.old.engine.input.KeyBinding
import bke.iso.old.engine.input.MouseBinding
import bke.iso.old.engine.math.Location
import bke.iso.old.engine.render.RenderService
import bke.iso.old.engine.state.State
import bke.iso.old.engine.system.System
import bke.iso.old.game.entity.BouncyBallSystem
import bke.iso.old.game.entity.BulletSystem
import bke.iso.old.game.entity.MovingPlatformSystem
import bke.iso.old.game.entity.PlayerSystem
import bke.iso.old.game.entity.TurretSystem
import bke.iso.old.game.combat.BulletCollisionHandler
import bke.iso.old.game.combat.DamageHandler
import bke.iso.old.game.combat.DrawHealthHandler
import bke.iso.old.game.combat.ShootHandler
import bke.iso.old.game.asset.GameMap
import bke.iso.old.game.asset.GameMapService
import bke.iso.old.game.entity.EntityFactory
import bke.iso.old.service.ServiceProvider
import com.badlogic.gdx.Input

class GameState(
    private val assetService: AssetService,
    private val gameMapService: GameMapService,
    private val entityFactory: EntityFactory,
    private val inputService: InputService,
    private val renderService: RenderService,
    private val systemProvider: ServiceProvider<System>,
    private val handlerProvider: ServiceProvider<EventHandler<*>>,
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
        entityFactory.createLampPost(Location(4, 4, 0))
        entityFactory.createLampPost(Location(8, 4, 0))

        entityFactory.createPillar(Location(12, 12, 0))
            .apply {
                x -= 0.5f
                y += 0.5f
            }
        entityFactory.createPillar(Location(10, 12, 0))
            .apply {
                x -= 0.5f
                y += 0.5f
            }
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
        inputService.bind("checkCollisions", KeyBinding(Input.Keys.C, InputState.DOWN))
    }

    private fun loadMap(mapName: String) {
        val gameMap = assetService.require<GameMap>(mapName)
        gameMapService.load(gameMap)
    }
}