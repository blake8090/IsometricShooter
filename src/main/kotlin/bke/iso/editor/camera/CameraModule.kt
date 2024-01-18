package bke.iso.editor.camera

import bke.iso.editor.ChangeSelectedLayerEvent
import bke.iso.editor.LayerModule
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging
import com.badlogic.gdx.Input as GdxInput

class ToggleHideWallsEvent : EditorEvent()

class CameraModule(
    private val renderer: Renderer,
    private val input: Input,
    private val editorScreen: EditorScreen,
    private val world: World,
    private val layerModule: LayerModule
) : Module {

    private val log = KotlinLogging.logger {}


    private val mouseDragAdapter = MouseDragAdapter(GdxInput.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)
    private lateinit var cameraActor: Actor

    private var wallsHidden = false

    fun init() {
        input.addInputProcessor(mouseDragAdapter)
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
        cameraActor.getCollisionBox()?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.CYAN)
        }

        if (editorScreen.hitMainView()) {
            panCamera()

            if (Gdx.input.isKeyPressed(GdxInput.Keys.C)) {
                moveCameraActor()
            }
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
        val pos = toWorld(renderer.getPointerPos(), z)
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
            renderer.setOcclusionTarget(cameraActor)
        } else {
            log.debug { "Showing walls" }
            renderer.setOcclusionTarget(null)
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
