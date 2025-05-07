package bke.iso.editor.actor

import bke.iso.editor.input.MouseDragAdapter
import bke.iso.editor.input.MouseScrollAdapter
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.sign

class CameraLogic(
    private val input: Input,
    private val renderer: Renderer,
) {

    private val mouseScrollAdapter = MouseScrollAdapter()
    private val cameraZoomIncrements = 0.25f
    private val mouseDragAdapter = MouseDragAdapter(com.badlogic.gdx.Input.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    fun start() {
        input.addInputProcessor(mouseDragAdapter)
        input.addInputProcessor(mouseScrollAdapter)

        input.keyMouse.bindKey("actorModeResetZoom", com.badlogic.gdx.Input.Keys.Q, ButtonState.PRESSED)
        input.keyMouse.bindKey("actorModeResetCamera", com.badlogic.gdx.Input.Keys.R, ButtonState.PRESSED)
    }

    fun stop() {
        input.removeInputProcessor(mouseDragAdapter)
        input.removeInputProcessor(mouseScrollAdapter)
    }

    fun update() {
        input.onAction("actorModeResetZoom") {
            renderer.resetCameraZoom()
        }

        input.onAction("actorModeResetCamera") {
            renderer.setCameraPos(Vector3())
        }

        mouseScrollAdapter.onScroll { _, y ->
            renderer.zoomCamera(cameraZoomIncrements * y.sign)
        }

        panCamera()
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
        renderer.moveCamera(cameraDelta)
    }
}
