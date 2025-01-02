package bke.iso.engine.loading

import bke.iso.engine.core.EngineModule
import io.github.oshai.kotlinlogging.KotlinLogging

class LoadingScreens : EngineModule() {

    override val moduleName: String = "loadingScreens"
    override val updateWhileLoading: Boolean = true
    override val profilingEnabled: Boolean = false

    private val log = KotlinLogging.logger { }

    private var currentScreen: LoadingScreen2? = null

    fun start(loadingScreen: LoadingScreen2, action: suspend () -> Unit) {
        log.debug { "Starting loading sequence with ${loadingScreen::class.simpleName}" }
        loadingScreen.start(action)
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
