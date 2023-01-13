package bke.iso.v2.engine

import bke.iso.engine.log
import bke.iso.service.Singleton
import bke.iso.v2.engine.entity.EntityService
import bke.iso.v2.engine.input.InputService
import bke.iso.v2.engine.render.RenderService
import bke.iso.v2.engine.state.StateService
import bke.iso.v2.engine.system.SystemService

@Singleton
class Engine(
    private val systemService: SystemService,
    private val stateService: StateService,
    private val renderService: RenderService,
    private val inputService: InputService,
    private val entityService: EntityService
) {

    fun start(game: Game) {
        log.info("Starting")
        game.setup()
        stateService.setState(game.initialState)
    }

    fun update(deltaTime: Float) {
        inputService.update()
        systemService.update(stateService.currentState, deltaTime)
        stateService.update(deltaTime)
        entityService.update()
        renderService.render()
    }

    fun stop() {
        log.info("Stopping")
    }
}
