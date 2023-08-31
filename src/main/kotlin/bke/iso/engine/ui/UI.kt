package bke.iso.engine.ui

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import mu.KotlinLogging

class UI(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}
    private val screens = ArrayDeque<UIScreen>()

    override fun update(deltaTime: Float) {
        for (screen in screens) {
            screen.render(deltaTime)
        }
    }

    override fun dispose() {
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
        game.input.addInputProcessor(screen.stage)
        game.input.addControllerListener(screen.controllerNavigation)
        screen.create()
        if (game.input.isUsingController()) {
            screen.controllerNavigation.start()
        }
    }

    private fun clear() {
        for (screen in screens) {
            log.debug { "Disposing screen ${screen::class.simpleName}" }
            game.input.removeInputProcessor(screen.stage)
            game.input.removeControllerListener(screen.controllerNavigation)
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
