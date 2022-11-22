package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import bke.iso.engine.assets.Assets
import bke.iso.engine.assets.TextureLoader
import bke.iso.engine.entity.Entities
import bke.iso.engine.event.EventService
import bke.iso.engine.event.TestEvent
import bke.iso.engine.event.TestEventHandler
import bke.iso.engine.input.Input
import bke.iso.engine.system.System
import kotlin.reflect.KClass

@Service
class Engine(
    private val services: Services,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input,
    private val entities: Entities
) {
    private var state: State = EmptyState()
    private val stateSystems = mutableListOf<System>()

    fun start(gameData: GameData) {
        log.info("Starting up")

        assets.addLoader("png", TextureLoader::class)
        assets.addLoader("jpg", TextureLoader::class)
        gameData.addAssetLoaders(assets)
        // TODO: loading screen?
        assets.load("assets")

        changeState(gameData.defaultState)

        testEventHandlers()
    }

    private fun testEventHandlers() {
        val eventService = services.get<EventService>()
        eventService.addHandler(TestEventHandler::class)
        eventService.fire(TestEvent("this is a test message!"))
    }

    /**
     * Main game loop
     */
    // TODO: set deltaTime in public var
    fun update(deltaTime: Float) {
        input.update()

        state.update(deltaTime)
        stateSystems.forEach { system -> system.update(deltaTime)  }
        entities.update()

        renderer.render()
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    // TODO: use setter
    fun changeState(newState: KClass<out State>) {
        log.debug(
            "Switching state from '${this.state::class.simpleName}' "
                    + "to '${newState.simpleName}'"
        )
        state.stop()
        state = services.createInstance(newState)

        stateSystems.clear()
        state.getSystems()
            .map { type -> services.createInstance(type) }
            .forEach { system ->
                stateSystems.add(system)
                log.debug("Added system '${system::class.simpleName}'")
            }

        state.start()
    }
}
