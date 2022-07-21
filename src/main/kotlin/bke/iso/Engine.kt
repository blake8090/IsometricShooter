package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.di.Singleton
import bke.iso.util.getLogger
import kotlin.reflect.KClass

@Singleton
class Engine(private val container: ServiceContainer) {
    private val log = getLogger()

    val eventHandlers = EventHandlers()
    private var state: State = EmptyState()

    fun start() {
        log.info("Starting up")
        log.debug("Current state is '${state.javaClass.simpleName}' by default")
    }

    // todo: have state react to emitted events
    fun update(deltaTime: Float) {
        state.input(deltaTime)
        state.updateSystems(deltaTime)
        state.update(deltaTime)
        state.draw(deltaTime)
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    fun changeState(stateType: KClass<State>) {
        val newState = container.createInstance(stateType)
        newState.setup(container)
        state.stop()
        state = newState
        newState.start()
        log.debug("Changed to state '${newState.javaClass.simpleName}")
    }
}
