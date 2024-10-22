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
        super.handleEvent(event)

        when (event) {
            is StartEvent -> startGame()
            is EditorEvent -> startEditor()
        }
    }

    private fun startGame() {
        game.ui.loadingScreen.start {
            loadGameAssets()
            game.states.setState<GameState>()
            game.events.fire(GameState.LoadSceneEvent("city2.scene", false))
        }
    }

    private fun startEditor() {
        game.ui.loadingScreen.start {
            loadGameAssets()
            game.states.setState<EditorState>()
        }
    }

    private suspend fun loadGameAssets() {
        game.assets.loadAsync("game")
        game.assets.shaders.compileAll()
    }

    class StartEvent : Event
    class EditorEvent : Event
}
