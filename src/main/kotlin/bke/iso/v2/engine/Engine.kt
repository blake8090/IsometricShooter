package bke.iso.v2.engine

import bke.iso.engine.log
import bke.iso.service.Singleton
import bke.iso.v2.engine.event.EventService
import bke.iso.v2.engine.input.InputService
import bke.iso.v2.engine.render.RenderService
import bke.iso.v2.engine.state.StateService
import bke.iso.v2.engine.system.SystemService

@Singleton
class Engine(
    private val systemService: SystemService,
    private val stateService: StateService,
    private val renderService: RenderService,
    private val eventService: EventService,
    private val inputService: InputService
) {

    fun start(game: Game) {
        log.info("Starting")
        eventService.start()
        game.setup()
        stateService.setState(game.initialState)
    }

    fun update(deltaTime: Float) {
        inputService.update()
        systemService.update(stateService.currentState, deltaTime)
        stateService.update(deltaTime)
        renderService.render()
    }

    fun stop() {
        log.info("Stopping")
    }
}
