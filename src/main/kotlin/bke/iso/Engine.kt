package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.util.getLogger

class Engine {
    private val log = getLogger()

    val container = ServiceContainer()

    init {
        container.registerFromClassPath("bke.iso")
    }

    fun start() {
        log.info("Starting up")
    }

    fun update(deltaTime: Float) {
        // todo: have state react to emitted events
        val stateService = container.getService<StateService>()
        stateService.withCurrentState { state -> state.input(deltaTime) }
        stateService.withCurrentState { state -> state.updateSystems(deltaTime) }
        stateService.withCurrentState { state -> state.update(deltaTime) }
        // todo: handle events here?
        stateService.withCurrentState { state -> state.draw(deltaTime) }
    }

    fun stop() {
        log.info("Shutting down")
    }
}
