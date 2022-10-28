package bke.iso.v2.engine

import bke.iso.engine.util.getLogger
import bke.iso.v2.app.service.Service
import bke.iso.v2.app.service.Services
import bke.iso.v2.engine.assets.Assets
import bke.iso.v2.engine.assets.TextureLoader
import kotlin.reflect.KClass

@Service
class Engine(private val services: Services) {
    private val log = getLogger()

    private var state: State = EmptyState()

    fun start(gameData: GameData) {
        log.info("Starting up")

        with(services.get<Assets>()) {
            addLoader("png", TextureLoader::class)
            addLoader("jpg", TextureLoader::class)
            gameData.addAssetLoaders(this)
            // TODO: loading screen?
            load("assets")
        }

        changeState(gameData.defaultState)
    }

    fun update(deltaTime: Float) {
        state.update(deltaTime)
        services.get<Renderer>().render()
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
