package bke.iso.editor

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    override val systems = emptySet<System>()

    private val editorScreen = EditorScreen()

    override fun load() {
        game.assets.load("game")
        game.assets.load("ui")
    }

    override fun start() {
        log.info { "Starting editor" }
        game.ui.setScreen(editorScreen)
    }
}
