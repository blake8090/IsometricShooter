package bke.iso.editor.actor

import bke.iso.editor.EditorMode
import bke.iso.editor.EditorModule
import bke.iso.editor.actor.command.AddComponentCommand
import bke.iso.editor.actor.command.DeleteComponentCommand
import bke.iso.editor.withFirstInstance
import bke.iso.engine.Engine
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Event
import bke.iso.engine.input.ButtonState
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

class ActorMode(private val engine: Engine) : EditorMode() {

    override val world = World(engine.events)
    override val renderer = Renderer(world, engine.world2, engine.assets, engine.events)

    private val log = KotlinLogging.logger { }

    private val cameraLogic = CameraLogic(engine.input, renderer)
    private val view = ActorModeView(engine.events, engine.assets)

    private var gridWidth = 5
    private var gridLength = 5

    private val referenceActor = world.actors.create(Vector3())
    private val components = mutableListOf<Component>()
    private var selectedComponent: Component? = null

    private val componentTypes = Reflections("bke.iso")
        .getSubTypesOf(Component::class.java)
        .map(Class<out Component>::kotlin)
        .filter { kClass -> kClass.hasAnnotation<Serializable>() }

    override fun start() {
        engine.ui.pushView(view)
        engine.rendererManager.setActiveRenderer(renderer)
        cameraLogic.start()

        with(engine.input.keyMouse) {
            bindKey("actorModeModifier", Input.Keys.CONTROL_LEFT, ButtonState.DOWN)
            bindKey("actorModeUndo", Input.Keys.Z, ButtonState.PRESSED)
            bindKey("actorModeRedo", Input.Keys.Y, ButtonState.PRESSED)
        }
    }

    override fun stop() {
        engine.ui.removeImGuiView(view)
        engine.rendererManager.reset()
        cameraLogic.stop()
    }

    override fun update() {
        cameraLogic.update()

        engine.input.onAction("sceneModeModifier") {
            engine.input.onAction("sceneModeUndo") {
                undo()
            }
            engine.input.onAction("sceneModeRedo") {
                redo()
            }
        }

        renderer.fgShapes.addPoint(referenceActor.pos, 1.25f, Color.RED)
        referenceActor.getCollisionBox()?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.CYAN)
            renderer.fgShapes.addPoint(box.pos, 1.5f, Color.CYAN)
        }

        drawGrid()

        view.viewData = ViewData(components, selectedComponent, componentTypes)
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

    override fun handleEvent(event: Event) {
        when (event) {
            is OpenClicked -> loadPrefab()
            is SaveClicked -> savePrefab()

            is ComponentSelected -> {
                selectedComponent = event.component
            }

            is SelectedComponentDeleted -> {
                val component = selectedComponent ?: return
                val command = DeleteComponentCommand(components, component)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is NewComponentTypeAdded -> {
                val command = AddComponentCommand(components, referenceActor, event.componentType)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }
        }
    }

    private fun loadPrefab() {
        val file = engine.dialogs.showOpenFileDialog("Actor Prefab", "actor") ?: return
        val prefab = engine.serializer.read<ActorPrefab>(file.readText())

        components.clear()
        components.addAll(prefab.components)

        referenceActor.components.clear()
        components.withFirstInstance<Sprite>(referenceActor::add)
        components.withFirstInstance<Collider>(referenceActor::add)

        resetCommands()
        log.info { "Loaded actor prefab: '${file.canonicalPath}'" }
    }

    private fun savePrefab() {
        val file = engine.dialogs.showSaveFileDialog("Actor Prefab", "actor") ?: return
        val name = file.nameWithoutExtension

        val content = engine.serializer.write(ActorPrefab(file.nameWithoutExtension, components))
        file.writeText(content)

        log.info { "Saved actor prefab: '$name'" }
    }

    class OpenClicked : Event

    class SaveClicked : Event

    data class ComponentSelected(val component: Component) : Event

    class SelectedComponentDeleted : Event

    data class NewComponentTypeAdded(val componentType: KClass<out Component>) : Event

    data class ViewData(
        val components: List<Component>,
        val selectedComponent: Component? = null,
        val componentTypes: List<KClass<out Component>>
    )
}
