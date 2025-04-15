package bke.iso.editor2.actor

import bke.iso.editor.withFirstInstance
import bke.iso.editor2.EditorMode
import bke.iso.engine.Engine
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Event
import bke.iso.engine.math.Box
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class ActorMode(private val engine: Engine) : EditorMode() {

    override val world = World(engine.events)
    override val renderer = Renderer(world, engine.assets, engine.events)

    private val log = KotlinLogging.logger { }

    private val cameraLogic = CameraLogic(engine.input, renderer)
    private val view = ActorModeView(engine.events)

    private var gridWidth = 5
    private var gridLength = 5

    private val referenceActor = world.actors.create(Vector3())
    private var selectedPrefab: ActorPrefab? = null

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
        drawPrefab()
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

    private fun drawPrefab() {
        selectedPrefab?.components?.withFirstInstance<Collider> { collider ->
            val min = referenceActor.pos.add(collider.offset)
            val max = Vector3(min).add(collider.size)
            val box = Box.fromMinMax(min, max)
            renderer.fgShapes.addBox(box, 0.75f, Color.CYAN)
            renderer.fgShapes.addPoint(box.pos, 1.5f, Color.CYAN)
        }
    }

    override fun draw() {
        view.draw(selectedPrefab?.components ?: emptyList())
    }

    override fun handleEvent(event: Event) {
        if (event is OpenClicked) {
            loadActorPrefab()
        }
    }

    private fun loadActorPrefab() {
        val file = engine.dialogs.showOpenFileDialog("Actor Prefab", "actor") ?: return
        val prefab = engine.serializer.read<ActorPrefab>(file.readText())
        selectedPrefab = prefab

        referenceActor.components.clear()
        prefab.components.withFirstInstance<Sprite> { sprite ->
            referenceActor.add(sprite)
        }

        view.reset()

        log.info { "Loaded actor prefab: '${file.canonicalPath}'" }
    }

    class OpenClicked : Event
}
