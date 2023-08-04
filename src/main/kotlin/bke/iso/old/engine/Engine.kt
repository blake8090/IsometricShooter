package bke.iso.old.engine

import bke.iso.old.engine.input.InputService
import bke.iso.old.engine.render.RenderService
import bke.iso.old.engine.state.StateService
import bke.iso.old.engine.system.SystemService
import bke.iso.old.engine.world.WorldService
import bke.iso.old.service.SingletonService

class Engine(
    private val systemService: SystemService,
    private val stateService: StateService,
    private val renderService: RenderService,
    private val inputService: InputService,
    private val worldService: WorldService
) : SingletonService {

    fun start(game: bke.iso.old.engine.Game) {
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
