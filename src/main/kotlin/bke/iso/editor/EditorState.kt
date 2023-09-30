package bke.iso.editor

import bke.iso.editor.main.EditorScreen
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    override val systems = emptySet<System>()

    private val log = KotlinLogging.logger {}

    private val editorScreen = EditorScreen(this, game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.RIGHT)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private val brushTool = BrushTool(game.world, game.renderer)

    override suspend fun load() {
        log.info { "Starting editor" }
        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)
        game.input.addInputProcessor(mouseDragAdapter)
    }

    fun handleEvent(event: EditorEvent) =
        when (event) {
            is TilePrefabSelectedEvent -> brushTool.selectPrefab(event.prefab)
            is ActorPrefabSelectedEvent -> brushTool.selectPrefab(event.prefab, event.sprite)
        }

    override fun update(deltaTime: Float) {
        drawGrid()
        panCamera()

        if (editorScreen.hitMainView()) {
            brushTool.update()
        }
    }

    private fun panCamera() {
        val delta = mouseDragAdapter.getDelta()
        if (delta.isZero) {
            return
        }
        val cameraDelta = Vector2(
            delta.x * cameraPanScale.x * -1, // for some reason the delta's x-axis is inverted!
            delta.y * cameraPanScale.y
        )
        game.renderer.moveCamera(cameraDelta)
    }

    private fun drawGrid() {
        for (x in 0..gridWidth) {
            game.renderer.shapes.addLine(
                Vector3(x.toFloat(), 0f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.GREEN
            )
        }

        for (y in 0..gridLength) {
            game.renderer.shapes.addLine(
                Vector3(0f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                0.5f,
                Color.GREEN
            )
        }
    }
}
