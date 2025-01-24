package bke.iso.editor.v3.scene

import bke.iso.editor.scene.camera.MouseDragAdapter
import bke.iso.editor.scene.camera.MouseScrollAdapter
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.core.Event
import bke.iso.engine.input.ButtonState
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.RendererManager
import bke.iso.engine.ui.v2.UIViewController
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.sign

class SceneTabViewController(
    view: SceneTabView,
    private val assets: Assets,
    private val renderer: Renderer,
    private val rendererManager: RendererManager,
    private val input: bke.iso.engine.input.Input
) : UIViewController<SceneTabView>(view) {

    private val log = KotlinLogging.logger { }

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private var selectedLayer = 0f

    private val mouseScrollAdapter = MouseScrollAdapter()
    private val cameraZoomIncrements = 0.25f
    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    override fun start() {
        log.debug { "Starting SceneTabViewController" }

        input.addInputProcessor(mouseDragAdapter)
        input.addInputProcessor(mouseScrollAdapter)
        with(input.keyMouse) {
            bindKey("sceneTabResetZoom", Input.Keys.Q, ButtonState.PRESSED)
            bindKey("sceneTabResetCamera", Input.Keys.R, ButtonState.PRESSED)
        }

        val assetList = mutableListOf<Any>()
        assetList.addAll(assets.getAll<TilePrefab>())
        assetList.addAll(assets.getAll<ActorPrefab>())
//        view.updateAssetBrowser(assetList)
    }

    override fun stop() {
        log.debug { "Stopping SceneTabViewController" }
    }

    override fun update(deltaTime: Float) {
        input.onAction("sceneTabResetZoom") {
            renderer.resetCameraZoom()
        }

        input.onAction("sceneTabResetCamera") {
            renderer.setCameraPos(Vector3())
        }

        mouseScrollAdapter.onScroll { _, y ->
            renderer.zoomCamera(cameraZoomIncrements * y.sign)
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

    override fun handleEvent(event: Event) {
        log.debug { "handling event ${event::class.simpleName}" }
    }

    override fun handleEvent(event: com.badlogic.gdx.scenes.scene2d.Event) {
//        log.debug { "handling event ${event::class.simpleName}" }
    }

    override fun enabled() {
        rendererManager.reset()
    }
}
