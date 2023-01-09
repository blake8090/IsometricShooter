package bke.iso.v2.engine.event

import bke.iso.service.Provider
import bke.iso.service.Singleton
import bke.iso.v2.engine.physics.MovementHandler
import bke.iso.v2.engine.state.StateService

@Singleton
class EventService(
    private val stateService: StateService,
    private val provider: Provider<EventHandler<*>>
) {

    private val baseEventHandlers = EventHandlerMap()

    fun start() {
        baseEventHandlers.add(provider.get(MovementHandler::class))
    }

    fun <T : Event> fire(event: T) {
        baseEventHandlers.fire(event)
        stateService.currentState.eventHandlers.fire(event)
    }
}
