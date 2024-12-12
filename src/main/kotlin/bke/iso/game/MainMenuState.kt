package bke.iso.game

import bke.iso.editor.EditorState
import bke.iso.engine.core.Event
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import bke.iso.engine.ui.loading.SimpleLoadingScreen
import bke.iso.game.ui.MainMenuScreen

class MainMenuState(override val engine: Engine) : State() {

    override val systems = linkedSetOf<System>()
    override val modules = emptySet<Module>()

    override suspend fun load() {
        engine.ui.setScreen(MainMenuScreen(engine.assets, engine.events))
        engine.ui.setLoadingScreen(SimpleLoadingScreen(engine.assets))
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        when (event) {
            is StartEvent -> startGame()
            is EditorEvent -> startEditor()
        }
    }

    private fun startGame() {
        engine.ui.loadingScreen.start {
            loadGameAssets()
            engine.states.setState<GameState>()
            engine.events.fire(GameState.LoadSceneEvent("mission-01-start.scene", false))
        }
    }

    private fun startEditor() {
        engine.ui.loadingScreen.start {
            loadGameAssets()
            engine.states.setState<EditorState>()
        }
    }

    private suspend fun loadGameAssets() {
        engine.assets.loadAsync("game")
        engine.assets.shaders.compileAll()
    }

    class StartEvent : Event
    class EditorEvent : Event
}
