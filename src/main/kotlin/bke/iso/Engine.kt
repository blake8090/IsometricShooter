package bke.iso

import bke.iso.di.ServiceContainer
import bke.iso.di.Singleton
import bke.iso.util.getLogger
import java.util.Stack
import kotlin.reflect.KClass

typealias EventHandler = (KClass<*>) -> Unit

open class Event

@Singleton
class Engine(private val container: ServiceContainer) {
    private val log = getLogger()

    var state = NoState()
        set(value) = changeState(value)

    private val events = Stack<Event>()
    private val handlers: Map<KClass<*>, EventHandler> = mutableMapOf()

    fun start() {
        log.info("Starting up")
    }

    // todo: have state react to emitted events
    fun update(deltaTime: Float) {
//        val stateService = container.getService<StateService>()
//        // todo:
//        stateService.withCurrentState { state ->
//            state.input(deltaTime)
//            state.updateSystems(deltaTime)
//            // todo: handle events here?
//            state.draw(deltaTime)
//        }
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    fun <T : Event> emitEvent(event: T) =
        events.push(event)

    fun handleEvent(handler: EventHandler) {

    }

    private fun changeState(state: State) {
        TODO("Not yet implemented")
    }
}

/*

engine {
    start()
    update()
    stop()
    setState()
    emitEvent()
    handleEvent()
}

 */
