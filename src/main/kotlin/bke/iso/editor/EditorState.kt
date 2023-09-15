package bke.iso.editor

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    override val systems = emptySet<System>()

    private val editorScreen = EditorScreen(game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.RIGHT)
    private val brushActor = game.world.actors.create(0f, 0f, 0f)

    override suspend fun load() {
        log.info { "Starting editor" }
        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)
        game.input.addInputProcessor(mouseDragAdapter)
    }

    override fun update(deltaTime: Float) {
        // TODO: event instead of poll?
        val selectedPrefab = editorScreen.assetBrowser.getSelectedPrefab()
        if (selectedPrefab != null) {
            brushActor.add(Sprite(
                texture = selectedPrefab.texture,
                offsetY = 16f // TODO: make this a constant
            ))
        } else {
            brushActor.remove<Sprite>()
        }
        drawGrid()

        // TODO: comment and break out of this method
        val delta = mouseDragAdapter.getDelta()
        if (!delta.isZero && editorScreen.hitMainView()) {
            game.renderer.moveCamera(delta.scl(-0.5f, 0.5f))
        }
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
