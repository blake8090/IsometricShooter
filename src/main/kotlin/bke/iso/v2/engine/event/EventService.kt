package bke.iso.v2.engine.event

import bke.iso.service.Singleton
import bke.iso.v2.engine.state.StateService

@Singleton
class EventService(private val stateService: StateService) {

    private val baseEventHandlers = EventHandlerMap()

    fun <T : Event> fire(event: T) {
        baseEventHandlers.fire(event)
        stateService.currentState.eventHandlers.fire(event)
    }
}
