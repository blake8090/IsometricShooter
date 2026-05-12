package bke.iso.editor.scene

import bke.iso.editor.core.input.MouseDragAdapter
import bke.iso.editor.core.input.MouseScrollAdapter
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.sign

class CameraLogic(
    private val input: Input,
    world: World,
    private val renderer: Renderer,
    private val sceneEditor: SceneEditor,
    private val collisionBoxes: CollisionBoxes
) {

    private val mouseScrollAdapter = MouseScrollAdapter()
    private val cameraZoomIncrements = 0.25f
    private val mouseDragAdapter = MouseDragAdapter(com.badlogic.gdx.Input.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private val cameraEntity =
        world.entities.create(
            Vector3(),
            Sprite(
                texture = "camera.png",
                offsetX = 12f,
                offsetY = 8f
            ),
            Collider(
                size = Vector3(0.5f, 0.5f, 1f),
                offset = Vector3(-0.25f, -0.25f, 0f),
            )
        )

    fun start() {
        input.addInputProcessor(mouseDragAdapter)
        input.addInputProcessor(mouseScrollAdapter)

        input.keyMouse.bindKey("sceneTabResetZoom", com.badlogic.gdx.Input.Keys.Q, ButtonState.PRESSED)
        input.keyMouse.bindKey("sceneTabResetCamera", com.badlogic.gdx.Input.Keys.R, ButtonState.PRESSED)
        input.keyMouse.bindKey("sceneTabCameraMoveCamera", com.badlogic.gdx.Input.Keys.C, ButtonState.PRESSED)
    }

    fun stop() {
        input.removeInputProcessor(mouseDragAdapter)
        input.removeInputProcessor(mouseScrollAdapter)
    }

    fun update() {
        input.onAction("sceneTabResetZoom") {
            renderer.resetCameraZoom()
        }

        input.onAction("sceneTabResetCamera") {
            renderer.setCameraPos(Vector3())
        }

        input.onAction("sceneTabCameraMoveCamera") {
            moveCameraEntity()
        }

        mouseScrollAdapter.onScroll { _, y ->
            renderer.zoomCamera(cameraZoomIncrements * y.sign)
        }

        collisionBoxes[cameraEntity]?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.CYAN)
        }

        panCamera()
    }

    private fun moveCameraEntity() {
        var z = sceneEditor.selectedLayer.toFloat()
        // apply a small offset so that the camera always appears above entities on the same z level
        if (z >= 0f) {
            z += 0.01f
        }

        val pos = toWorld(renderer.pointer.pos, z)
        cameraEntity.moveTo(pos.x, pos.y, pos.z)
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

    fun setCameraAsOcclusionTarget() {
        renderer.occlusion.target = cameraEntity
    }
}
