package bke.iso.editor.v3.scene

import bke.iso.editor.v3.scene.world.WorldLogic
import bke.iso.engine.Engine
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.ui.v2.UIViewController
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Event
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneTabViewController(
    view: SceneTabView,
    private val engine: Engine
) : UIViewController<SceneTabView>(view) {

    private val log = KotlinLogging.logger { }

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    var selectedLayer = 0f
        private set
    private var hideUpperLayers = false
    private var highlightLayer = false

    private val cameraLogic =
        CameraLogic(
            this,
            engine.input,
            engine.world,
            engine.renderer
        )

    private val worldLogic =
        WorldLogic(
            engine.world,
            engine.assets,
            engine.events,
            engine.dialogs,
            engine.serializer
        )

    override fun start() {
        log.debug { "Starting SceneTabViewController" }

        cameraLogic.start()

        val assetList = mutableListOf<Any>()
        assetList.addAll(engine.assets.getAll<TilePrefab>())
        assetList.addAll(engine.assets.getAll<ActorPrefab>())
        view.assetBrowserView.refresh(assetList)

        view.toolbarView.refreshLayerLabel(selectedLayer)
    }

    override fun stop() {
        log.debug { "Stopping SceneTabViewController" }
        cameraLogic.stop()
    }

    override fun enabled() {
        engine.rendererManager.reset()
    }

    override fun update(deltaTime: Float) {
        cameraLogic.update()
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
            engine.renderer.fgShapes
        } else {
            engine.renderer.bgShapes
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

            is SceneTabView.OnOpenMenuButtonClicked -> {
                worldLogic.loadScene()
            }
        }
    }
}
