package bke.iso.v2.engine

import bke.iso.engine.util.getLogger
import bke.iso.v2.app.service.Service
import bke.iso.v2.app.service.Services
import kotlin.reflect.KClass

@Service
class Engine(private val services: Services) {
    private val log = getLogger()

    private var state: State = EmptyState()

    fun start(gameData: GameData) {
        log.info("Starting up")
        // TODO: add asset loaders here
        changeState(gameData.defaultState)
    }

    fun update(deltaTime: Float) {
        state.update(deltaTime)
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
