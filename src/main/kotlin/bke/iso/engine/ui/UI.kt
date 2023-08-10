package bke.iso.engine.ui

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.Module

class UI(override val game: Game) : Module() {

    private val screens = ArrayDeque<UIScreen>()

    override fun update(deltaTime: Float) {
        screens.forEach { screen -> screen.render(deltaTime) }
    }

    override fun stop() {
        screens.forEach(UIScreen::dispose)
    }

    fun resize(width: Int, height: Int) {
        screens.forEach { screen -> screen.resize(width, height) }
    }

    fun setScreen(screen: UIScreen) {
        screens.forEach(UIScreen::dispose)
        screens.clear()
        screens.addFirst(screen)
    }

    fun handleEvent(event: Event) {
        screens.forEach { screen -> screen.handleEvent(event) }
    }
}
