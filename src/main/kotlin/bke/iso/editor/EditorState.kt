package bke.iso.editor

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    override val systems = emptySet<System>()

    private val editorScreen = EditorScreen(game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val lastCursorPos = Vector2()

    override suspend fun load() {
        game.assets.loadAsync("game")
        log.info { "Starting editor" }
        game.ui.setScreen(editorScreen)
    }

    override fun update(deltaTime: Float) {
        // TODO: refactor all this
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            lastCursorPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            val pos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())

            val difference = Vector2(
                (pos.x - lastCursorPos.x) * -1, // movement seems to be inverted on the x-axis
                pos.y - lastCursorPos.y,
            )
            if (!difference.isZero && editorScreen.hitMainView()) {
                log.trace { "dragged: $difference" }
                game.renderer.moveCamera(difference)
            }

            lastCursorPos.set(pos)
        }
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
