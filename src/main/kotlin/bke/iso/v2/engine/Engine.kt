package bke.iso.v2.engine

import bke.iso.engine.log
import bke.iso.service.Singleton
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.asset.TextureLoader
import bke.iso.v2.engine.render.RenderService
import bke.iso.v2.engine.state.StateService

@Singleton
class Engine(
    private val assetService: AssetService,
    private val stateService: StateService,
    private val renderService: RenderService
) {

    fun start(game: Game) {
        log.info("Starting")
        assetService.addLoader("png", TextureLoader::class)
        assetService.addLoader("jpg", TextureLoader::class)
        game.setup()
        stateService.setState(game.initialState)
    }

    fun update(deltaTime: Float) {
        stateService.update(deltaTime)
        renderService.render()
    }

    fun stop() {
        log.info("Stopping")
    }
}
