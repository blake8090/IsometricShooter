package bke.iso.engine.ui

import bke.iso.engine.Event
import bke.iso.engine.input.Input
import bke.iso.engine.ui.loading.LoadingScreen
import mu.KotlinLogging

class UI(private val input: Input) {

    private val log = KotlinLogging.logger {}
    private val screens = ArrayDeque<UIScreen>()

    lateinit var loadingScreen: LoadingScreen
        private set

    val isLoadingScreenActive: Boolean
        get() = loadingScreen.active

    fun setScreen(screen: UIScreen) {
        clear()
        log.debug { "Setting screen to ${screen::class.simpleName}" }
        screens.addFirst(screen)
        input.addInputProcessor(screen.stage)
        input.addControllerListener(screen.controllerNavigation)
        screen.create()
        if (input.isUsingController()) {
            screen.controllerNavigation.start()
        }
    }

    private fun clear() {
        for (screen in screens) {
            log.debug { "Disposing screen ${screen::class.simpleName}" }
            input.removeInputProcessor(screen.stage)
            input.removeControllerListener(screen.controllerNavigation)
            screen.dispose()
        }
        screens.clear()
    }

    fun setLoadingScreen(screen: LoadingScreen) {
        log.debug { "Setting loading screen to '${screen::class.simpleName}'" }
        screen.create()
        this.loadingScreen = screen
        log.debug { "Set loading screen to '${screen::class.simpleName}'" }
    }

    fun draw(deltaTime: Float) {
        if (loadingScreen.active) {
            loadingScreen.draw(deltaTime)
            return
        }

        for (screen in screens) {
            screen.draw(deltaTime)
        }
    }

    fun resize(width: Int, height: Int) {
        for (screen in screens) {
            screen.resize(width, height)
        }
    }

    fun handleEvent(event: Event) {
        for (screen in screens) {
            screen.handleEvent(event)
        }
    }

    fun dispose() {
        for (screen in screens) {
            screen.dispose()
        }
    }
}
