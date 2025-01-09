package bke.iso.editor.v2.actor

import bke.iso.editor.scene.camera.MouseDragAdapter
import bke.iso.editor.scene.camera.MouseScrollAdapter
import bke.iso.editor.v2.core.EditorViewController
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.core.Events
import bke.iso.engine.core.Module
import bke.iso.engine.input.Input
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.RendererManager
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.oshai.kotlinlogging.KotlinLogging
import com.badlogic.gdx.Input as GdxInput

class ActorTabViewController(
    skin: Skin,
    assets: Assets,
    events: Events,
    private val input: Input,
) : EditorViewController<ActorTabView>() {

    private val log = KotlinLogging.logger { }

    override val modules: Set<Module> = emptySet()
    override val view: ActorTabView = ActorTabView(skin, assets)

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
        createReferenceActor()
    }

    private fun createReferenceActor() {
        referenceActor = world.actors.create(Vector3())
    }

    override fun stop() {
        log.debug { "Stopping ActorTabViewController" }
        renderer.stop()
    }

    override fun update(deltaTime: Float) {
        panCamera()
        drawGrid()
        drawReferenceActorPos()
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

    fun enableRenderer(rendererManager: RendererManager) {
        rendererManager.setActiveRenderer(renderer)
    }

}
