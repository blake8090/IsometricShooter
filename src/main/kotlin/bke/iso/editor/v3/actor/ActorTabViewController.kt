package bke.iso.editor.v3.actor

import bke.iso.editor.scene.camera.MouseDragAdapter
import bke.iso.editor.scene.camera.MouseScrollAdapter
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Events
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.Input
import bke.iso.engine.math.Box
import bke.iso.engine.os.Dialogs
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.RendererManager
import bke.iso.engine.render.Sprite
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.ui.v2.UIViewController
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.sign
import com.badlogic.gdx.Input as GdxInput
import com.badlogic.gdx.scenes.scene2d.Event as GdxEvent

class ActorTabViewController(
    view: ActorTabView,
    assets: Assets,
    events: Events,
    private val input: Input,
    private val rendererManager: RendererManager,
    private val dialogs: Dialogs,
    private val serializer: Serializer
) : UIViewController<ActorTabView>(view) {

    private val log = KotlinLogging.logger { }

    private val world = World(events)
    private val renderer = Renderer(world, assets, events)

    private val gridWidth = 5
    private val gridLength = 5

    private val mouseScrollAdapter = MouseScrollAdapter()
    private val cameraZoomIncrements = 0.25f
    private val mouseDragAdapter = MouseDragAdapter(GdxInput.Buttons.MIDDLE)
    private val cameraPanScale = Vector2(0.5f, 0.5f)

    private lateinit var selectedPrefab: ActorPrefab
    private lateinit var referenceActor: Actor

    override fun start() {
        log.debug { "Starting ActorTabViewController" }

        input.addInputProcessor(mouseDragAdapter)
        input.addInputProcessor(mouseScrollAdapter)
        with(input.keyMouse) {
            bindKey("actorTabResetZoom", GdxInput.Keys.Q, ButtonState.PRESSED)
            bindKey("actorTabResetCamera", GdxInput.Keys.R, ButtonState.PRESSED)
        }

        selectedPrefab = ActorPrefab("", mutableListOf())
        referenceActor = world.actors.create(Vector3())
    }

    override fun stop() {
        log.debug { "stop" }
    }

    override fun update(deltaTime: Float) {
        input.onAction("actorTabResetZoom") {
            renderer.resetCameraZoom()
        }

        input.onAction("actorTabResetCamera") {
            renderer.setCameraPos(Vector3())
        }

        mouseScrollAdapter.onScroll { _, y ->
            renderer.zoomCamera(cameraZoomIncrements * y.sign)
        }

        panCamera()
        drawGrid()
        drawPrefab()
        drawReferenceActorPos()
    }

    private fun drawPrefab() {
        selectedPrefab.components.withFirstInstance<Collider> { collider ->
            val min = referenceActor.pos.add(collider.offset)
            val max = Vector3(min).add(collider.size)
            val box = Box.fromMinMax(min, max)
            renderer.fgShapes.addBox(box, 0.75f, Color.CYAN)
            renderer.fgShapes.addPoint(box.pos, 1.5f, Color.CYAN)
        }
    }

    private fun drawReferenceActorPos() {
        renderer.fgShapes.addPoint(referenceActor.pos, 1.25f, Color.RED)
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

    override fun handleEvent(event: GdxEvent) {
        when (event) {
            is ComponentBrowserView.OnAddButtonClicked -> {
                log.debug { "adding component" }
            }

            is ComponentBrowserView.OnDeleteButtonClicked -> {
                deleteComponent(view.componentBrowserView.getCheckedIndex())
            }

            is ComponentBrowserView.OnSelectComponent -> {
                log.debug { "selected component ${event.component::class.simpleName}" }
                view.updateComponentInspector(event.component)
            }

            is ActorTabView.OnOpenClicked -> {
                openPrefab()
            }
        }
    }

    private fun openPrefab() {
        val file = dialogs.showOpenFileDialog("Actor Prefab", "actor") ?: return
        val prefab = serializer.read<ActorPrefab>(file.readText())
        loadPrefab(prefab)
        log.info { "Loaded actor prefab: '${file.canonicalPath}'" }
    }

    private fun loadPrefab(prefab: ActorPrefab) {
        selectedPrefab = prefab

        referenceActor.components.clear()
        selectedPrefab.components.withFirstInstance<Sprite> { sprite ->
            referenceActor.add(sprite)
        }

        view.updateComponentBrowser(selectedPrefab.components)
    }

    private fun deleteComponent(index: Int) {
        if (index < 0 || index >= selectedPrefab.components.size) {
            log.debug { "invalid component index $index" }
            return
        }

        val component = selectedPrefab.components[index]
        selectedPrefab.components.removeAt(index)
        log.debug { "Deleted component ${component::class.simpleName}" }
        view.updateComponentBrowser(selectedPrefab.components)
    }

    override fun enabled() {
        rendererManager.setActiveRenderer(renderer)
    }
}
