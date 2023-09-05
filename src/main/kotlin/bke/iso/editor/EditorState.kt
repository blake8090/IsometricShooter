package bke.iso.editor

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    override val systems = emptySet<System>()

    private val editorScreen = EditorScreen()

    private var gridWidth = 20
    private var gridLength = 20

    override fun load() {
        game.assets.load("game")
        game.assets.load("ui")
    }

    override fun start() {
        log.info { "Starting editor" }
        game.ui.setScreen(editorScreen)
    }

    override fun update(deltaTime: Float) {
        drawGrid()
    }

    private fun drawGrid() {
        for (x in 0..gridWidth) {
            game.renderer.shapes.addLine(
                Vector3(x.toFloat(), 0f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                1f,
                Color.GREEN
            )
        }

        for (y in 0..gridLength) {
            game.renderer.shapes.addLine(
                Vector3(0f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                1f,
                Color.GREEN
            )
        }
    }
}
