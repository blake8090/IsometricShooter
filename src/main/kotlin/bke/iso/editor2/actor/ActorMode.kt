package bke.iso.editor2.actor

import bke.iso.editor2.EditorMode
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

class ActorMode(private val engine: Engine) : EditorMode() {

    override val world = World(engine.events)
    override val renderer = Renderer(world, engine.assets, engine.events)

    private val cameraLogic = CameraLogic(engine.input, renderer)
    private val view = ActorModeView(engine.events)

    private var gridWidth = 5
    private var gridLength = 5

    private val referenceActor = world.actors.create(Vector3())

    override fun start() {
        engine.rendererManager.setActiveRenderer(renderer)
        cameraLogic.start()
    }

    override fun stop() {
        engine.rendererManager.reset()
        cameraLogic.stop()
    }

    override fun update() {
        cameraLogic.update()

        renderer.fgShapes.addPoint(referenceActor.pos, 1.25f, Color.RED)
        drawGrid()
    }

    private fun drawGrid() {
        for (x in -5..5) {
            renderer.bgShapes.addLine(
                Vector3(x.toFloat(), -5f, 0f),
                Vector3(x.toFloat(), gridLength.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }

        for (y in -5..5) {
            renderer.bgShapes.addLine(
                Vector3(-5f, y.toFloat(), 0f),
                Vector3(gridWidth.toFloat(), y.toFloat(), 0f),
                0.5f,
                Color.WHITE
            )
        }
    }

    override fun draw() {
        view.draw()
    }

    override fun handleEvent(event: Event) {}
}
