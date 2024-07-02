package bke.iso.game

import bke.iso.editor.EditorState
import bke.iso.engine.Event
import bke.iso.engine.Game
import bke.iso.engine.state.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import bke.iso.engine.ui.loading.SimpleLoadingScreen
import bke.iso.game.ui.MainMenuScreen

class MainMenuState(override val game: Game) : State() {

    override val systems = linkedSetOf<System>()
    override val modules = emptySet<Module>()

    override suspend fun load() {
        game.ui.setScreen(MainMenuScreen(game.assets, game.events))
        game.ui.setLoadingScreen(SimpleLoadingScreen(game.assets))
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is StartEvent -> {
                game.ui.loadingScreen.start {
                    game.states.setState<GameState>()
                    game.events.fire(GameState.LoadSceneEvent("mission-01-roof.scene", false))
                }
            }

            is EditorEvent -> {
                game.states.setState<EditorState>()
            }
        }
    }

    class StartEvent : Event
    class EditorEvent : Event
}
