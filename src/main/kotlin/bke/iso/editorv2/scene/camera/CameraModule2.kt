package bke.iso.editorv2.scene.camera

import bke.iso.editor.event.EditorEvent
import bke.iso.editorv2.scene.layer.ChangeSelectedLayerEvent
import bke.iso.editorv2.scene.layer.LayerModule2
import bke.iso.editorv2.scene.ui.SceneTabView
import bke.iso.engine.Event
import bke.iso.engine.state.Module
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.sign
import com.badlogic.gdx.Input as GdxInput

class ToggleHideWallsEvent : EditorEvent()

class CameraModule2(
    private val renderer: Renderer,
    private val input: Input,
    private val world: World,
    private val layerModule: LayerModule2,
    private val sceneTabView: SceneTabView
) : Module {

    private val log = KotlinLogging.logger {}

    private val mouseScrollAdapter = MouseScrollAdapter2()
    private val cameraZoomIncrements = 0.25f

    private val mouseDragAdapter = MouseDragAdapter2(GdxInput.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)
    private lateinit var cameraActor: Actor

    private var wallsHidden = false

    fun init() {
        input.addInputProcessor(mouseDragAdapter)
        input.addInputProcessor(mouseScrollAdapter)
        initCamera()
    }

    private fun initCamera() {
        cameraActor = world.actors.create(
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
    }

    override fun update(deltaTime: Float) {
        if (sceneTabView.hitTouchableArea()) {

            panCamera()

            input.onAction("moveCamera") {
                moveCameraActor()
            }

            input.onAction("resetZoom") {
                renderer.resetCameraZoom()
            }

            mouseScrollAdapter.onScroll { _, y ->
                renderer.zoomCamera(cameraZoomIncrements * y.sign)
            }
        }
    }

    fun draw() {
        cameraActor.getCollisionBox()?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.CYAN)
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
        renderer.moveCamera(cameraDelta)
    }

    private fun moveCameraActor() {
        val z = getZ(layerModule.selectedLayer.toFloat())
        val pos = toWorld(renderer.pointer.pos, z)
        cameraActor.moveTo(pos.x, pos.y, pos.z)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is ToggleHideWallsEvent -> {
                toggleWallsHidden()
            }

            is ChangeSelectedLayerEvent -> {
                updateCameraActorZ(event.selectedLayer)
            }
        }
    }

    private fun toggleWallsHidden() {
        wallsHidden = !wallsHidden
        if (wallsHidden) {
            log.debug { "Hiding walls" }
            renderer.occlusion.target = cameraActor
        } else {
            log.debug { "Showing walls" }
            renderer.occlusion.target = null
        }
    }

    private fun updateCameraActorZ(selectedLayer: Float) {
        cameraActor.moveTo(cameraActor.x, cameraActor.y, getZ(selectedLayer))
    }

    private fun getZ(selectedLayer: Float): Float {
        var z = selectedLayer
        // apply a small offset so that the camera always appears above actors on the same z level
        if (z >= 0f) {
            z += 0.01f
        }
        return z
    }
}
