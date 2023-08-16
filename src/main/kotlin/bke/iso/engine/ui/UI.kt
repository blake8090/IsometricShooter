package bke.iso.engine.ui

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module
import mu.KotlinLogging

class UI(override val game: Game) : Module() {

    private val log = KotlinLogging.logger {}
    private val screens = ArrayDeque<UIScreen>()

    override fun update(deltaTime: Float) {
        screens.forEach { screen -> screen.render(deltaTime) }
    }

    override fun dispose() {
        screens.forEach(UIScreen::dispose)
    }

    fun resize(width: Int, height: Int) {
        screens.forEach { screen -> screen.resize(width, height) }
    }

    fun setScreen(screen: UIScreen) {
        log.debug { "setting screen to ${screen::class.simpleName}" }
        clear()
        screens.addFirst(screen)
        game.input.addInputProcessor(screen.stage)
        game.input.addControllerListener(screen.controllerNavigation)
        screen.create()
        if (game.input.isUsingController()) {
            screen.controllerNavigation.start()
        }
    }

    private fun clear() {
        screens.forEach { screen ->
            game.input.removeInputProcessor(screen.stage)
            game.input.removeControllerListener(screen.controllerNavigation)
            screen.dispose()
        }
        screens.clear()
    }

    fun handleEvent(event: Event) {
        screens.forEach { screen -> screen.handleEvent(event) }
    }
}
