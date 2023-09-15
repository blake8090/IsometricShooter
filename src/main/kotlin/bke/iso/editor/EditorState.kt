package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class EditorState(override val game: Game) : State() {

    private val log = KotlinLogging.logger {}

    override val systems = emptySet<System>()

    private val editorScreen = EditorScreen(this, game.assets)
    private var gridWidth = 20
    private var gridLength = 20

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.RIGHT)
    private val referenceActor = game.world.actors.create(0f, 0f, 0f)

    override suspend fun load() {
        log.info { "Starting editor" }
        game.assets.loadAsync("game")
        game.ui.setScreen(editorScreen)
        game.input.addInputProcessor(mouseDragAdapter)
    }

    fun handleEvent(event: EditorEvent) =
        when(event) {
            is TilePrefabSelectedEvent -> {
                log.debug { "tile prefab '${event.prefab.name}' selected" }
                val sprite = Sprite(event.prefab.texture, 0f, 16f)
                referenceActor.add(sprite)
            }

            is ActorPrefabSelectedEvent -> {
                log.debug { "tile prefab '${event.prefab.name}' selected" }
                referenceActor.add(event.sprite.copy())
            }
        }

    override fun update(deltaTime: Float) {
        drawGrid()

        // TODO: comment and break out of this method
        val delta = mouseDragAdapter.getDelta()
        if (!delta.isZero && editorScreen.hitMainView()) {
            game.renderer.moveCamera(delta.scl(-0.5f, 0.5f))
        }

        if (editorScreen.hitMainView()) {
            val pos = toWorld(game.renderer.getCursorPos())
            // TODO: scale position when screen size changes
            referenceActor.moveTo(pos.x, pos.y, pos.z)
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
