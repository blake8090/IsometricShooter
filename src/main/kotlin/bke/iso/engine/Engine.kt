package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import bke.iso.engine.assets.Assets
import bke.iso.engine.assets.TextureLoader
import bke.iso.engine.event.EventService
import bke.iso.engine.event.TestEvent
import bke.iso.engine.event.TestEventHandler
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import kotlin.reflect.KClass

@Service
class Engine(
    private val services: Services,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input
) {
    var deltaTime: Float = 0f
        private set

    private var state: State = EmptyState()

    fun start(game: Game) {
        log.info("Starting up")
        game.setup()
        assets.addLoader("png", TextureLoader::class)
        assets.addLoader("jpg", TextureLoader::class)
        assets.load("assets")
        changeState(game.initialState)
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
    fun update(deltaTime: Float) {
        this.deltaTime = deltaTime
        input.update()
        state.update(deltaTime)
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
    }
}
