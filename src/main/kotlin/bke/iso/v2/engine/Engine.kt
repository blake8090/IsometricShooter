package bke.iso.v2.engine

import bke.iso.engine.log
import bke.iso.service.Singleton
import bke.iso.v2.engine.render.RenderService
import bke.iso.v2.engine.state.StateService

@Singleton
class Engine(
    private val stateService: StateService,
    private val renderService: RenderService
) {

    fun start() {
        log.info("Starting")
    }

    fun update(deltaTime: Float) {
        stateService.update(deltaTime)
        renderService.render()
    }

    fun stop() {
        log.info("Stopping")
    }
}