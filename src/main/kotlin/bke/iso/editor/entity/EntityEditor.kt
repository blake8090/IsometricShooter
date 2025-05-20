package bke.iso.editor.entity

import bke.iso.editor.EditorModule
import bke.iso.editor.core.BaseEditor
import bke.iso.editor.entity.command.AddComponentCommand
import bke.iso.editor.entity.command.DeleteComponentCommand
import bke.iso.editor.withFirstInstance
import bke.iso.engine.Engine
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.collision.Collider
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Event
import bke.iso.engine.input.ButtonState
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Component
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

class EntityEditor(private val engine: Engine) : BaseEditor() {

    override val world = World(engine.events)
    override val renderer = Renderer(world, engine.assets, engine.events)

    private val log = KotlinLogging.logger { }

    private val cameraLogic = CameraLogic(engine.input, renderer)
    private val view = EntityModeView(engine.events, engine.assets)

    private var gridWidth = 5
    private var gridLength = 5

    private val referenceEntity = world.entities.create(Vector3())
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
            bindKey("entityModeModifier", Input.Keys.CONTROL_LEFT, ButtonState.DOWN)
            bindKey("entityModeUndo", Input.Keys.Z, ButtonState.PRESSED)
            bindKey("entityModeRedo", Input.Keys.Y, ButtonState.PRESSED)
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
                commands.undo()
            }
            engine.input.onAction("sceneModeRedo") {
                commands.redo()
            }
        }

        renderer.fgShapes.addPoint(referenceEntity.pos, 1.25f, Color.RED)
        referenceEntity.getCollisionBox()?.let { box ->
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
            is OpenClicked -> loadTemplate()
            is SaveClicked -> saveTemplate()

            is ComponentSelected -> {
                selectedComponent = event.component
            }

            is SelectedComponentDeleted -> {
                val component = selectedComponent ?: return
                val command = DeleteComponentCommand(components, component)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is NewComponentTypeAdded -> {
                val command = AddComponentCommand(components, referenceEntity, event.componentType)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }
        }
    }

    private fun loadTemplate() {
        val file = engine.dialogs.showOpenFileDialog("Entity Template", "entity") ?: return
        val template = engine.serializer.read<EntityTemplate>(file.readText())

        components.clear()
        components.addAll(template.components)

        referenceEntity.components.clear()
        components.withFirstInstance<Sprite>(referenceEntity::add)
        components.withFirstInstance<Collider>(referenceEntity::add)

        commands.reset()
        log.info { "Loaded entity template: '${file.canonicalPath}'" }
    }

    private fun saveTemplate() {
        val file = engine.dialogs.showSaveFileDialog("Entity Template", "entity") ?: return
        val name = file.nameWithoutExtension

        val content = engine.serializer.write(EntityTemplate(file.nameWithoutExtension, components))
        file.writeText(content)

        log.info { "Saved entity template: '$name'" }
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
