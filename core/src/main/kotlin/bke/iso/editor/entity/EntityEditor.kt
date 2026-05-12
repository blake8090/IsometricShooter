package bke.iso.editor.entity

import bke.iso.editor.EditorModule
import bke.iso.editor.core.ComponentEditorView
import bke.iso.editor.core.BaseEditor
import bke.iso.editor.core.command.AddComponentCommand
import bke.iso.editor.core.command.DeleteComponentCommand
import bke.iso.editor.core.command.PutMapEntryCommand
import bke.iso.editor.core.command.RemoveMapEntryCommand
import bke.iso.editor.core.command.UpdatePropertyCommand
import bke.iso.editor.withFirstInstance
import bke.iso.engine.Engine
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Event
import bke.iso.engine.input.ButtonState
import bke.iso.engine.lighting.FullBright
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
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation

class EntityEditor(private val engine: Engine) : BaseEditor() {

    override val world = World(engine.events, engine.collisionBoxes)
    override val renderer = Renderer(world, engine.assets, engine.lighting, engine.events, engine.collisionBoxes)

    private val log = KotlinLogging.logger { }

    private val cameraLogic = CameraLogic(engine.input, renderer)
    private val view = EntityEditorView(engine.events, engine.assets)
        .apply {
            validateComponentType = ::validateComponentType
        }

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
        renderer.bgColor = Color.GRAY
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
        engine.collisionBoxes[referenceEntity]?.let { box ->
            renderer.fgShapes.addBox(box, 0.375f, Color.CYAN)
            renderer.fgShapes.addPoint(box.pos, 1.0f, Color.CYAN)
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

            is NewComponentTypeAdded -> {
                val component = event.componentType.createInstance()
                val command = AddComponentCommand(components, component, ::refreshComponents)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is ComponentDeleted -> {
                val command = DeleteComponentCommand(components, event.component, ::refreshComponents)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is ComponentEditorView.PropertyUpdated<*> -> {
                val command = UpdatePropertyCommand(
                    instance = event.component,
                    property = event.property,
                    newValue = event.newValue
                )
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is ComponentEditorView.MapEntryAdded<*, *> -> {
                val command = PutMapEntryCommand(event.map as MutableMap<Any, Any>, event.key, event.value)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is ComponentEditorView.MapEntryRemoved<*, *> -> {
                val command = RemoveMapEntryCommand(event.map as MutableMap<Any, Any>, event.key)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }
        }
    }

    private fun loadTemplate() {
        val file = engine.dialogs.showOpenFileDialog("Entity Template", "entity") ?: return
        val template = engine.serializer.read<EntityTemplate>(file.readText())

        components.clear()
        components.addAll(template.components)

        refreshComponents()

        commands.reset()
        log.info { "Loaded entity template: '${file.canonicalPath}'" }
    }

    private fun refreshComponents() {
        referenceEntity.components.clear()
        components.withFirstInstance<Sprite>(referenceEntity::add)
        components.withFirstInstance<Collider>(referenceEntity::add)
        // hack to disable lighting - right now multiple renderers share the same lighting!
        referenceEntity.add(FullBright())
    }

    private fun saveTemplate() {
        val file = engine.dialogs.showSaveFileDialog("Entity Template", "entity") ?: return
        val name = file.nameWithoutExtension

        val content = engine.serializer.write(EntityTemplate(file.nameWithoutExtension, components))
        file.writeText(content)

        log.info { "Saved entity template: '$name'" }
    }

    private fun validateComponentType(type: KClass<out Component>): Boolean {
        val currentTypes = components.map { component -> component::class }
        return currentTypes.contains(type)
    }

    class OpenClicked : Event

    class SaveClicked : Event

    data class ComponentSelected(val component: Component) : Event

    data class ComponentDeleted(val component: Component) : Event

    data class NewComponentTypeAdded(val componentType: KClass<out Component>) : Event

    data class ViewData(
        val components: List<Component>,
        val selectedComponent: Component? = null,
        val componentTypes: List<KClass<out Component>>
    )
}
