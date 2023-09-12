package bke.iso.engine.ui

import bke.iso.engine.Event
import bke.iso.engine.input.Input
import mu.KotlinLogging

class UI(private val input: Input) {

    private val log = KotlinLogging.logger {}
    private val screens = ArrayDeque<UIScreen>()

    fun update(deltaTime: Float) {
        for (screen in screens) {
            screen.render(deltaTime)
        }
    }

    fun dispose() {
        for (screen in screens) {
            screen.dispose()
        }
    }

    fun resize(width: Int, height: Int) {
        for (screen in screens) {
            screen.resize(width, height)
        }
    }

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

    fun handleEvent(event: Event) {
        for (screen in screens) {
            screen.handleEvent(event)
        }
    }
}
