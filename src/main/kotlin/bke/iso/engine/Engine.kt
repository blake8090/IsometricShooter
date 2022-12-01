package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import bke.iso.engine.assets.Assets
import bke.iso.engine.assets.TextureLoader
import bke.iso.engine.event.EventService
import bke.iso.engine.input.Input
import bke.iso.engine.physics.MovementHandler
import bke.iso.engine.physics.PhysicsController
import bke.iso.engine.render.Renderer
import kotlin.reflect.KClass

@Service
class Engine(
    private val services: Services,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input,
) {
    var deltaTime: Float = 0f
        private set

    private var state: State = EmptyState()

    private val engineControllers = mutableSetOf<Controller>()
    private val stateControllers = mutableSetOf<Controller>()

    fun start(game: Game) {
        log.info("Starting up")
        setupEventHandlers()
        setupControllers()
        setupAssetLoaders()

        log.debug("initializing game '${game::class.simpleName}'")
        game.setup()

        assets.load("assets")
        changeState(game.initialState)
    }

    private fun setupEventHandlers() {
        val eventService = services.get<EventService>()
        eventService.addHandler(MovementHandler::class)
    }

    private fun setupControllers() {
        engineControllers.add(services.createInstance(PhysicsController::class))
    }

    private fun setupAssetLoaders() {
        assets.addLoader("png", TextureLoader::class)
        assets.addLoader("jpg", TextureLoader::class)
    }

    /**
     * Main game loop
     */
    fun update(deltaTime: Float) {
        this.deltaTime = deltaTime
        input.update()

        engineControllers.forEach { controller -> controller.update(deltaTime) }
        stateControllers.forEach { controller -> controller.update(deltaTime) }

        renderer.render()
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    fun changeState(newState: KClass<out State>) {
        log.debug(
            "Switching state from '${this.state::class.simpleName}' "
                    + "to '${newState.simpleName}'"
        )
        state.stop()
        state = services.createInstance(newState)
        state.start()

        stateControllers.clear()
        state.controllers
            .map { type -> services.createInstance(type) }
            .forEach(stateControllers::add)
    }
}
