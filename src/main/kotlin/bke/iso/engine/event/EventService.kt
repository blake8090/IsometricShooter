package bke.iso.engine.event

import bke.iso.service.Provider
import bke.iso.service.Singleton
import bke.iso.engine.physics.MovementHandler
import bke.iso.engine.state.StateService

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
