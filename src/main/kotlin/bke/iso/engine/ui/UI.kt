package bke.iso.engine.ui

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

    fun pushScreen(screen: UIScreen) {
        screen.create()
        screens.addFirst(screen)
    }
}
