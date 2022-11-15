package bke.iso.engine

import bke.iso.app.service.Service
import bke.iso.app.service.Services
import bke.iso.engine.assets.Assets
import bke.iso.engine.assets.TextureLoader
import bke.iso.engine.input.Input
import bke.iso.engine.physics.Physics
import kotlin.reflect.KClass

@Service
class Engine(
    private val services: Services,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input,
    private val physics: Physics
) {
    private var state: State = EmptyState()

    fun start(gameData: GameData) {
        log.info("Starting up")

        assets.addLoader("png", TextureLoader::class)
        assets.addLoader("jpg", TextureLoader::class)
        gameData.addAssetLoaders(assets)
        // TODO: loading screen?
        assets.load("assets")

        changeState(gameData.defaultState)
    }

    fun update(deltaTime: Float) {
        input.update()
        state.update(deltaTime)
        physics.update(deltaTime)
        renderer.render()
    }

    fun stop() {
        log.info("Shutting down")
        state.stop()
    }

    fun changeState(newState: KClass<out State>) {
        log.debug(
            "Switching state from '${this.state::class.simpleName}' "
                    + "to '${newState.simpleName}'"
        )
        state.stop()
        state = services.createInstance(newState)
        state.start()
    }
}
