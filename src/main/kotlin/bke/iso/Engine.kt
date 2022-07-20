package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.di.Singleton
import bke.iso.system.System
import bke.iso.util.getLogger
import kotlin.reflect.KClass

@Singleton
class Engine(private val container: ServiceContainer) {
    private val log = getLogger()

    private val systems: MutableSet<System> = mutableSetOf()
    private var state: State = EmptyState()

    fun start() {
        log.info("Starting up")
        log.debug("Current state is '${state.javaClass.simpleName}' by default")
    }

    // todo: have state react to emitted events
    fun update(deltaTime: Float) {
        state.input(deltaTime)
        state.update(deltaTime)
        state.draw(deltaTime)
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    fun changeState(stateType: KClass<State>) {
        log.debug("Changing state from '${state.javaClass.simpleName}' to '${stateType.simpleName}'")

        val newState = container.createInstance(stateType)
        val newSystems = newState.getSystems()
            .map(container::createInstance)

        state.stop()
        newState.start()
        systems.clear()
        systems.addAll(newSystems)

        state = newState
        log.debug("Changed to state '${newState.javaClass.simpleName}")
    }
}
