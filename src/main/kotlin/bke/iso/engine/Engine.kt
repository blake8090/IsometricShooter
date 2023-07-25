package bke.iso.engine

import bke.iso.engine.input.InputService
import bke.iso.engine.render.RenderService
import bke.iso.engine.state.StateService
import bke.iso.engine.system.SystemService
import bke.iso.engine.world.WorldService
import bke.iso.service.SingletonService

class Engine(
    private val systemService: SystemService,
    private val stateService: StateService,
    private val renderService: RenderService,
    private val inputService: InputService,
    private val worldService: WorldService
) : SingletonService {

    fun start(game: Game) {
        log.info("Starting")
        game.setup()
        stateService.setState(game.initialState)
    }

    fun update(deltaTime: Float) {
        inputService.update()
        systemService.update(deltaTime)
        stateService.update(deltaTime)
        worldService.update()

        renderService.render()
        systemService.onFrameEnd()
    }

    fun stop() {
        log.info("Stopping")
    }
}
