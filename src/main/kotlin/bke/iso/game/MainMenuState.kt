package bke.iso.game

import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.GameState
import bke.iso.engine.System
import bke.iso.game.ui.MainMenuScreen

class MainMenuState(override val game: Game) : GameState() {

    override val systems: Set<System> = emptySet()

    override fun start() {
        game.assets.load("ui")
        game.ui.setScreen(MainMenuScreen(game.assets, game.events))
    }

    override fun handleEvent(event: Event) {
        if (event is StartEvent) {
            game.switchState(GameplayState::class)
        }
    }

    class StartEvent : Event()
}
