package bke.iso.editor.camera

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.Input as GdxInput

class CameraModule(
    private val renderer: Renderer,
    private val input: Input,
    private val editorScreen: EditorScreen
) : Module {

    private val mouseDragAdapter = MouseDragAdapter(GdxInput.Buttons.RIGHT)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    override fun update(deltaTime: Float) {
        if (!editorScreen.hitMainView()) {
            return
        }

        val delta = mouseDragAdapter.getDelta()
        if (delta.isZero) {
            return
        }
        val cameraDelta = Vector2(
            delta.x * cameraPanScale.x * -1, // for some reason the delta's x-axis is inverted!
            delta.y * cameraPanScale.y
        )
        renderer.moveCamera(cameraDelta)
    }

    override fun handleEvent(event: Event) {
    }

    fun init() {
        input.addInputProcessor(mouseDragAdapter)
    }
}
