package bke.iso.game

import bke.iso.editor.EditorState
import bke.iso.engine.core.Event
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.loading.EmptyLoadingScreen
import bke.iso.engine.loading.SimpleLoadingScreen
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import bke.iso.game.ui.MainMenuScreen

class MainMenuState(override val engine: Engine) : State() {

    override val systems = linkedSetOf<System>()
    override val modules = emptySet<Module>()

    override suspend fun load() {
        engine.ui.setScreen(MainMenuScreen(engine.assets, engine.events))
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        when (event) {
            is StartEvent -> startGame()
            is EditorEvent -> startEditor()
        }
    }

    private fun startGame() {
        engine.loadingScreens.start(SimpleLoadingScreen(engine.assets)) {
            loadGameAssets()
            engine.states.setState<GameState>()
            engine.events.fire(GameState.LoadSceneEvent("mission-01-start.scene", false))
        }
    }

    private fun startEditor() {
        engine.loadingScreens.start(EmptyLoadingScreen()) {
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
