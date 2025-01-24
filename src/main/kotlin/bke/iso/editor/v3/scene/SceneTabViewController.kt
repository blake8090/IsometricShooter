package bke.iso.editor.v3.scene

import bke.iso.editor.scene.camera.MouseDragAdapter
import bke.iso.editor.scene.camera.MouseScrollAdapter
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.math.toWorld
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.RendererManager
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.v2.UIViewController
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.sign
import com.badlogic.gdx.Input as GdxInput

class SceneTabViewController(
    view: SceneTabView,
    private val assets: Assets,
    private val renderer: Renderer,
    private val rendererManager: RendererManager,
    private val input: Input,
    world: World
) : UIViewController<SceneTabView>(view) {

    private val log = KotlinLogging.logger { }

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private var selectedLayer = 0f
    private var hideUpperLayers = false
    private var highlightLayer = false

    private val mouseScrollAdapter = MouseScrollAdapter()
    private val cameraZoomIncrements = 0.25f
    private val mouseDragAdapter = MouseDragAdapter(GdxInput.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private val cameraActor =
        world.actors.create(
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

    override fun start() {
        log.debug { "Starting SceneTabViewController" }

        input.addInputProcessor(mouseDragAdapter)
        input.addInputProcessor(mouseScrollAdapter)

        input.keyMouse.bindKey("sceneTabResetZoom", GdxInput.Keys.Q, ButtonState.PRESSED)
        input.keyMouse.bindKey("sceneTabResetCamera", GdxInput.Keys.R, ButtonState.PRESSED)
        input.keyMouse.bindKey("sceneTabCameraMoveCamera", GdxInput.Keys.C, ButtonState.PRESSED)

        val assetList = mutableListOf<Any>()
        assetList.addAll(assets.getAll<TilePrefab>())
        assetList.addAll(assets.getAll<ActorPrefab>())
        view.assetBrowserView.refresh(assetList)

        view.toolbarView.refreshLayerLabel(selectedLayer)
    }

    override fun stop() {
        log.debug { "Stopping SceneTabViewController" }
        input.removeInputProcessor(mouseDragAdapter)
        input.removeInputProcessor(mouseScrollAdapter)
    }

    override fun enabled() {
        rendererManager.reset()
    }

    override fun update(deltaTime: Float) {
        input.onAction("sceneTabResetZoom") {
            renderer.resetCameraZoom()
        }

        input.onAction("sceneTabResetCamera") {
            renderer.setCameraPos(Vector3())
        }

        input.onAction("sceneTabCameraMoveCamera") {
            moveCameraActor()
        }

        mouseScrollAdapter.onScroll { _, y ->
            renderer.zoomCamera(cameraZoomIncrements * y.sign)
        }

        cameraActor.getCollisionBox()?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.CYAN)
        }

        panCamera()
        drawGrid()
    }

    private fun drawGrid() {
        val shapes = getShapesArray()

        for (x in 0..gridWidth) {
            shapes.addLine(
                Vector3(x.toFloat(), 0f, selectedLayer),
                Vector3(x.toFloat(), gridLength.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
        for (y in 0..gridLength) {
            shapes.addLine(
                Vector3(0f, y.toFloat(), selectedLayer),
                Vector3(gridWidth.toFloat(), y.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
    }

    private fun getShapesArray() =
        if (drawGridForeground) {
            renderer.fgShapes
        } else {
            renderer.bgShapes
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
        var z = selectedLayer
        // apply a small offset so that the camera always appears above actors on the same z level
        if (z >= 0f) {
            z += 0.01f
        }

        val pos = toWorld(renderer.pointer.pos, z)
        cameraActor.moveTo(pos.x, pos.y, pos.z)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is ToolbarView.OnDecreaseLayerButtonClicked -> {
                selectedLayer--
                view.toolbarView.refreshLayerLabel(selectedLayer)
            }

            is ToolbarView.OnIncreaseLayerButtonClicked -> {
                selectedLayer++
                view.toolbarView.refreshLayerLabel(selectedLayer)
            }
        }
    }
}
