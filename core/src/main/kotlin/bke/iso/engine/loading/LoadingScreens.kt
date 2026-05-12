package bke.iso.engine.loading

import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import io.github.oshai.kotlinlogging.KotlinLogging

class LoadActionCompleteEvent : Event

class LoadingScreens(private val events: Events) : EngineModule() {

    override val moduleName = "loadingScreens"
    override val updateWhileLoading = true
    override val profilingEnabled = false

    private val log = KotlinLogging.logger { }

    private var currentScreen: LoadingScreen? = null

    fun start(loadingScreen: LoadingScreen, action: suspend () -> Unit) {
        log.debug { "Starting loading sequence with ${loadingScreen::class.simpleName}" }
        loadingScreen.start(action)
        loadingScreen.onActionComplete = {
            events.fire(LoadActionCompleteEvent())
        }
        currentScreen = loadingScreen
    }

    override fun update(deltaTime: Float) {
        currentScreen?.update(deltaTime)
    }

    fun isLoading() =
        currentScreen
            ?.active
            ?: false
}
