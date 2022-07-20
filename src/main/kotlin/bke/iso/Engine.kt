package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.di.Singleton
import bke.iso.util.getLogger
import java.util.Stack
import kotlin.reflect.KClass

@Singleton
class Engine(private val container: ServiceContainer) {
    private val log = getLogger()

    private var state: State = NoState()

    private val events = Stack<Event>()
    private val handlers: Map<KClass<*>, EventHandler> = mutableMapOf()

    fun start() {
        log.info("Starting up")
    }

    // todo: have state react to emitted events
    fun update(deltaTime: Float) {
        state.input(deltaTime)
        state.updateSystems(deltaTime)
        // todo: handle events here?
        state.draw(deltaTime)
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    fun setState(newState: State) {
        log.debug("Setting state ${state.javaClass.simpleName} to ${newState.javaClass.simpleName}")
        state.stop()
        state = newState
        newState.start()
    }

    fun <T : Event> emitEvent(event: T) =
        events.push(event)

    fun handleEvent(handler: EventHandler) {
    }
}
