package bke.iso.game

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.game.ui.MainMenuScreen

class MainMenuState(override val game: Game) : State() {

    override val systems: Set<System> = emptySet()

    override fun start() {
        game.assets.load("ui")
        game.ui.setScreen(MainMenuScreen(game.assets, game.events))
    }

    override fun handleEvent(event: Event) {
        if (event is StartEvent) {
            game.switchState(GameState::class)
        }
    }

    class StartEvent : Event()
}
