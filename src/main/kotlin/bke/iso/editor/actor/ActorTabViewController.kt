package bke.iso.editor.actor

import bke.iso.editor.scene.camera.MouseDragAdapter
import bke.iso.editor.scene.camera.MouseScrollAdapter
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.input.ButtonState
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.sign

class ActorTabViewController(
    private val engine: Engine,
    private val actorTabView: ActorTabView
) {

    private val log = KotlinLogging.logger {}

    private val world = World(engine.events)
    private val actorTabRenderer = Renderer(world, engine.assets, engine.events)

    private var gridWidth = 5
    private var gridLength = 5

    private val mouseScrollAdapter = MouseScrollAdapter()
    private val cameraZoomIncrements = 0.25f

    private val mouseDragAdapter = MouseDragAdapter(Input.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private val actorModule = ActorModule(
        engine.dialogs,
        engine.serializer,
        world,
        actorTabRenderer
    )

    fun init() {
        engine.input.addInputProcessor(mouseDragAdapter)
        engine.input.addInputProcessor(mouseScrollAdapter)

        with(engine.input.keyMouse) {
            bindKey("actorTabResetZoom", Input.Keys.Q, ButtonState.PRESSED)
            bindKey("actorTabResetCamera", Input.Keys.R, ButtonState.PRESSED)
        }
    }

    fun getModules(): Set<Module> = setOf(
        actorModule
    )

    fun enable() {
        engine.rendererManager.setActiveRenderer(actorTabRenderer)
    }

    fun update() {
        engine.input.onAction("actorTabResetZoom") {
            actorTabRenderer.resetCameraZoom()
        }

        engine.input.onAction("actorTabResetCamera") {
            actorTabRenderer.setCameraPos(Vector3())
        }

        panCamera()

        mouseScrollAdapter.onScroll { _, y ->
            actorTabRenderer.zoomCamera(cameraZoomIncrements * y.sign)
        }

        drawGrid()
    }

    private fun drawGrid() {
        for (x in -5..5) {
            actorTabRenderer.bgShapes.addLine(
                Vector3(x.toFloat(), -5f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }

        for (y in -5..5) {
            actorTabRenderer.bgShapes.addLine(
                Vector3(-5f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
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
        actorTabRenderer.moveCamera(cameraDelta)
    }
}
