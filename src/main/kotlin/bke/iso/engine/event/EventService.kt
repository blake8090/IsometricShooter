package bke.iso.engine.event

import bke.iso.engine.state.StateService
import bke.iso.service.ServiceProvider
import bke.iso.service.SingletonService

class EventService(
    private val stateService: StateService,
    private val provider: ServiceProvider<EventHandler<*>>
) : SingletonService {

    private val baseEventHandlers = EventHandlerMap()

    fun <T : Event> fire(event: T) {
        baseEventHandlers.fire(event)
        stateService.currentState.eventHandlers.fire(event)
    }
}
