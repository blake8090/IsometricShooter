package bke.iso.editor.v2.scene

import bke.iso.editor.v2.core.EditorViewController
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.core.Module
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneTabViewController(
    skin: Skin,
    private val assets: Assets,
    private val renderer: Renderer,
) : EditorViewController<SceneTabView>() {

    private val log = KotlinLogging.logger { }

    override val modules: Set<Module> = emptySet()
    override val view: SceneTabView = SceneTabView(skin, assets)

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private var selectedLayer = 0f

    override fun start() {
        log.debug { "Starting SceneTabViewController" }
        val assetList = mutableListOf<Any>()
        assetList.addAll(assets.getAll<TilePrefab>())
        assetList.addAll(assets.getAll<ActorPrefab>())
        view.updateAssetBrowser(assetList)
    }

    override fun stop() {
        log.debug { "Stopping SceneTabViewController" }
    }

    override fun update(deltaTime: Float) {
        drawGrid()
    }

    private fun drawGrid() {
        val shapes = getShapesArray()

        for (x in -10..gridWidth) {
            shapes.addLine(
                Vector3(x.toFloat(), 0f, selectedLayer),
                Vector3(x.toFloat(), gridLength.toFloat(), selectedLayer),
                0.5f,
                Color.WHITE
            )
        }
        for (y in -10..gridLength) {
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
}
