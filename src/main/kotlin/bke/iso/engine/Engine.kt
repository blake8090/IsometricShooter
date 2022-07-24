package bke.iso.engine

import bke.iso.engine.di.ServiceContainer
import bke.iso.engine.di.Singleton
import bke.iso.engine.util.getLogger
import kotlin.reflect.KClass

@Singleton
class Engine(private val container: ServiceContainer) {
    private val log = getLogger()

    val eventHandlers = EventHandlers()
    private var state: State = EmptyState()

    fun start() {
        log.info("Starting up")
        log.debug("Current state is '${state.javaClass.simpleName}' by default")

        val gameInfo = container.getService<GameInfo>()
        changeState(gameInfo.defaultState)
    }

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

    fun <T : Event> emitEvent(caller: Any, eventType: KClass<T>, event: T) {
        log.debug("Class '${caller.javaClass.simpleName}' is emitting event '$event' of type '${eventType.simpleName}'")
        state.eventHandlers.run(event, eventType)
        eventHandlers.run(event, eventType)
    }

    inline fun <reified T : Event> emitEvent(caller: Any, event: T) =
        emitEvent(caller, T::class, event)

    fun changeState(stateType: KClass<out State>) {
        val newState = container.createInstance(stateType)
        newState.setup(container)
        state.stop()
        state = newState
        newState.start()
        log.debug("Changed to state '${newState.javaClass.simpleName}")
    }
}
