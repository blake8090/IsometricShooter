package bke.iso.old.engine.event

import bke.iso.old.engine.state.StateService
import bke.iso.old.service.ServiceProvider
import bke.iso.old.service.SingletonService

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
