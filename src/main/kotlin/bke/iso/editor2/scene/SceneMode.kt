package bke.iso.editor2.scene

import bke.iso.editor2.EditorMode
import bke.iso.engine.asset.Assets
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneMode(
    renderer: Renderer,
    world: World,
    assets: Assets,
    input: Input
) : EditorMode(renderer, world) {

    private val log = KotlinLogging.logger { }

    var selectedLayer = 0f
        private set

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private val cameraLogic = CameraLogic(input, world, renderer, this)
    private val view = SceneModeView(assets)

    override fun start() {
        cameraLogic.start()
    }

    override fun stop() {
        cameraLogic.stop()
    }

    override fun update() {
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
            renderer.fgShapes
        } else {
            renderer.bgShapes
        }


    override fun draw() {
        view.draw()
    }
}
