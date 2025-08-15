package bke.iso.editor.scene

import bke.iso.editor.EditorModule
import bke.iso.editor.color
import bke.iso.editor.core.BaseEditor
import bke.iso.editor.scene.command.AddTagCommand
import bke.iso.editor.scene.command.AssignBuildingCommand
import bke.iso.editor.scene.command.DeleteTagCommand
import bke.iso.editor.scene.command.DisableComponentOverrideCommand
import bke.iso.editor.scene.command.EnableComponentOverrideCommand
import bke.iso.editor.scene.command.UpdateInstancePropertyCommand
import bke.iso.editor.scene.tool.ToolLogic
import bke.iso.editor.scene.tool.ToolSelection
import bke.iso.editor.scene.view.SceneEditorView
import bke.iso.engine.Engine
import bke.iso.engine.asset.BASE_PATH
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Event
import bke.iso.engine.imGuiWantsToCaptureInput
import bke.iso.engine.input.ButtonState
import bke.iso.engine.math.Box
import bke.iso.engine.scene.Scene
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Tags
import bke.iso.engine.world.event.EntityCreated
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString
import java.io.File
import kotlin.reflect.KMutableProperty1

class SceneEditor(private val engine: Engine) : BaseEditor() {

    override val world = engine.world
    override val renderer = engine.renderer

    private val log = KotlinLogging.logger { }

    var selectedLayer = 0
        private set

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private val cameraLogic = CameraLogic(engine.input, world, renderer, this)
    private val worldLogic = WorldLogic(world, engine.assets, engine.events, engine.lighting)
    private val toolLogic =
        ToolLogic(this, engine.input, engine.collisions, engine.renderer, engine.events, world, worldLogic)

    private val view = SceneEditorView(engine.assets, engine.events)

    private var hideWalls = false
    var hideUpperLayers = false
        private set
    private var highlightSelectedLayer = false

    private var selectedEntity: Entity? = null
    private var selectedBuilding: String? = null

    private val buildingFont = engine.assets.fonts[FontOptions("roboto.ttf", 12f, Color.WHITE)]

    private var selectedAssetDirectory: File? = null
    private val entityTemplatesInDirectory: Array<EntityTemplate> = Array()

    override fun start() {
        engine.ui.pushView(view)
        cameraLogic.start()
        toolLogic.start()

        with(engine.input.keyMouse) {
            bindMouse("sceneTabToolDown", Input.Buttons.LEFT, ButtonState.DOWN)
            bindMouse("sceneTabToolPress", Input.Buttons.LEFT, ButtonState.PRESSED)
            bindMouse("sceneTabToolRelease", Input.Buttons.LEFT, ButtonState.RELEASED)

            bindKey("sceneModeModifier", Input.Keys.CONTROL_LEFT, ButtonState.DOWN)
            bindKey("sceneModeUndo", Input.Keys.Z, ButtonState.PRESSED)
            bindKey("sceneModeRedo", Input.Keys.Y, ButtonState.PRESSED)
        }

//        renderer.occlusion.resetStrategies()
        renderer.bgColor = Color.GRAY
        // TODO: set a var instead of passing this - can make everything private
        renderer.occlusion.addStrategy(UpperLayerOcclusionStrategy(this))

        // show all assets by default
        selectAssetDirectory(File(BASE_PATH))
    }

    override fun stop() {
        engine.ui.removeImGuiView(view)
        cameraLogic.stop()
    }

    override fun update() {
        cameraLogic.update()

        drawGrid()
        drawSelectedEntity()

        for (buildingName in world.buildings.getAll()) {
            drawBuilding(buildingName)
        }

        engine.world.entities.each<Tags>(::drawEntityTags)

        drawComponentOverrideHints()

        toolLogic.update()

        if (!imGuiWantsToCaptureInput()) {
            engine.input.onAction("sceneTabToolPress") {
                toolLogic.performAction()?.let { command ->
                    engine.events.fire(EditorModule.ExecuteCommand(command))
                }
            }

            engine.input.onAction("sceneTabToolDown") {
                toolLogic.performMultiAction()?.let { command ->
                    engine.events.fire(EditorModule.ExecuteCommand(command))
                }
            }

            engine.input.onAction("sceneTabToolRelease") {
                toolLogic.performReleaseAction()?.let { command ->
                    engine.events.fire(EditorModule.ExecuteCommand(command))
                }
            }
        }

        engine.input.onAction("sceneModeModifier") {
            engine.input.onAction("sceneModeUndo") {
                commands.undo()
            }
            engine.input.onAction("sceneModeRedo") {
                commands.redo()
            }
        }

        view.viewData =
            ViewData(
                selectedEntity = selectedEntity,
                selectedEntityData =
                    if (selectedEntity != null) {
                        worldLogic.getData(selectedEntity!!)
                    } else {
                        null
                    },

                selectedTool = toolLogic.selection,
                selectedLayer = selectedLayer,
                hideWalls = hideWalls,
                hideUpperLayers = hideUpperLayers,
                highlightSelectedLayer = highlightSelectedLayer,
                buildings = engine.world.buildings.getAll(),
                selectedBuilding = selectedBuilding,
                messageBarText = getMessageBarText(),
                selectedAssetDirectory = selectedAssetDirectory,
                entityTemplatesInDirectory = entityTemplatesInDirectory
            )
    }

    private fun drawGrid() {
        val shapes = getShapesArray()

        for (x in 0..gridWidth) {
            shapes.addLine(
                Vector3(x.toFloat(), 0f, selectedLayer.toFloat()),
                Vector3(x.toFloat(), gridLength.toFloat(), selectedLayer.toFloat()),
                0.5f,
                Color.WHITE
            )
        }
        for (y in 0..gridLength) {
            shapes.addLine(
                Vector3(0f, y.toFloat(), selectedLayer.toFloat()),
                Vector3(gridWidth.toFloat(), y.toFloat(), selectedLayer.toFloat()),
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

    private fun drawSelectedEntity() {
        val entity = selectedEntity ?: return
        val collisionBox = entity.getCollisionBox() ?: Box(entity.pos, Vector3(1f, 1f, 1f))
        renderer.fgShapes.addBox(collisionBox, 1f, Color.RED)
    }

    private fun drawEntityTags(entity: Entity, tags: Tags) {
        if (tags.tags.isEmpty() || selectedEntity == entity) {
            return
        }
        entity.getCollisionBox()?.let { box ->
            engine.renderer.fgShapes.addBox(box, 1f, color(46, 125, 50))
        }
    }

    private fun drawComponentOverrideHints() {
        for (referenceEntity in worldLogic.getReferenceEntities()) {
            val data = worldLogic.getData(referenceEntity)

            if (selectedEntity != referenceEntity && data.componentOverrides.isNotEmpty()) {
                referenceEntity.getCollisionBox()?.let { box ->
                    engine.renderer.fgShapes.addBox(box, 1f, color(46, 125, 50))
                }
            }
        }
    }

    private fun drawBuilding(name: String) {
        val box = world.buildings.getBounds(name) ?: return

        if (box.min.z > selectedLayer && hideUpperLayers) {
            return
        }

        val color =
            if (selectedBuilding == name) {
                Color.WHITE
            } else {
                Color.BLUE
            }

        renderer.fgShapes.addBox(box, 1.25f, color)
        renderer.drawText(name, buildingFont, box.pos)
    }

    private fun getMessageBarText(): String =
        if (selectedBuilding.isNullOrBlank()) {
            "No building selected"
        } else {
            "Editing building $selectedBuilding"
        }

    override fun handleEvent(event: Event) {
        when (event) {
            is OpenSceneClicked -> openScene()
            is SaveSceneClicked -> saveScene()
            is SceneLoaded -> commands.reset()

            is ToolSelected -> toolLogic.selectTool(event.selection)
            is EntityTemplateSelected -> toolLogic.onEntityTemplateSelected(event.template)
            is EntitySelected -> selectedEntity = event.entity

            is LayerIncreased -> selectedLayer++
            is LayerDecreased -> selectedLayer--

            is HideWallsToggled -> toggleHideWalls()
            is HideUpperLayersToggled -> hideUpperLayers = !hideUpperLayers
            is HighlightSelectedLayerToggled -> highlightSelectedLayer = !highlightSelectedLayer

            is TagAdded -> {
                val command = AddTagCommand(event.entity, event.tag)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is TagDeleted -> {
                val command = DeleteTagCommand(event.entity, event.tag)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is BuildingAssigned -> {
                val command = AssignBuildingCommand(event.entity, event.building, worldLogic)
                engine.events.fire(EditorModule.ExecuteCommand(command))
            }

            is BuildingSelected -> {
                selectedBuilding = event.building
            }

            is BuildingClosed -> {
                selectedBuilding = null
            }

            is BuildingDeleted -> {
                log.debug { "deleting ${event.building}" }
            }

            is BuildingCreated -> {
                log.debug { "creating new building ${event.building}" }
                selectedBuilding = event.building
            }

            is EntityCreated -> { // TODO: could we do this in the command instead?
                worldLogic.setBuilding(event.entity, selectedBuilding)
            }

            is AssetDirectorySelected -> {
                log.debug { "Selected asset directory '${event.dir.path}'" }
                selectAssetDirectory(event.dir)
            }

            is ComponentOverrideEnabled -> {
                val command = EnableComponentOverrideCommand(
                    engine.serializer,
                    worldLogic,
                    event.referenceEntity,
                    event.templateComponent
                )
                engine.events.fire(EditorModule.ExecuteCommand(command))
                worldLogic.refreshComponents(event.referenceEntity)
            }

            is ComponentOverrideDisabled -> {
                val command = DisableComponentOverrideCommand(
                    worldLogic,
                    event.referenceEntity,
                    event.componentOverride
                )
                engine.events.fire(EditorModule.ExecuteCommand(command))
                worldLogic.refreshComponents(event.referenceEntity)
            }

            is PropertyUpdated<*> -> {
                val command = UpdateInstancePropertyCommand(
                    instance = event.component,
                    property = event.property,
                    newValue = event.newValue
                )
                engine.events.fire(EditorModule.ExecuteCommand(command))
                selectedEntity?.let(worldLogic::refreshComponents)
            }
        }
    }

    private fun openScene() {
        val file = engine.dialogs.showOpenFileDialog("Scene", "scene") ?: return
        val scene = engine.serializer.read<Scene>(file.readText())

        // very important to clear the state before loading a new scene.
        // otherwise, events fired in the load process will use the old state, leading to weird bugs
        selectedLayer = 0
        selectedEntity = null
        selectedBuilding = null

        worldLogic.loadScene(scene)
        log.info { "Loaded scene: '${file.canonicalPath}'" }
    }

    private fun saveScene() {
        val file = engine.dialogs.showSaveFileDialog("Scene", "scene") ?: return

        val scene = worldLogic.createScene()
        val content = engine.serializer.format.encodeToString(scene)
        file.writeText(content)
        log.info { "Saved scene: '${file.canonicalPath}'" }
    }

    private fun toggleHideWalls() {
        hideWalls = !hideWalls
        if (hideWalls) {
            log.debug { "Hiding walls" }
            cameraLogic.setCameraAsOcclusionTarget()
        } else {
            log.debug { "Showing walls" }
            renderer.occlusion.target = null
        }
    }

    private fun selectAssetDirectory(dir: File) {
        selectedAssetDirectory = dir
        entityTemplatesInDirectory.clear()
        dir
            .walk()
            .filter(File::isFile)
            .filter { file -> file.extension == "entity" }
            .map { file -> engine.assets.get<EntityTemplate>(file.nameWithoutExtension) }
            .forEach(entityTemplatesInDirectory::add)
    }

    class OpenSceneClicked : Event
    class SaveSceneClicked : Event
    class SceneLoaded : Event

    data class ToolSelected(val selection: ToolSelection) : Event
    data class EntityTemplateSelected(val template: EntityTemplate) : Event
    data class EntitySelected(val entity: Entity) : Event

    class LayerIncreased : Event
    class LayerDecreased : Event

    class HideWallsToggled : Event
    class HideUpperLayersToggled : Event
    class HighlightSelectedLayerToggled : Event

    class TagAdded(val entity: Entity, val tag: String) : Event
    data class TagDeleted(val entity: Entity, val tag: String) : Event

    data class BuildingAssigned(val entity: Entity, val building: String?) : Event
    data class BuildingSelected(val building: String) : Event
    class BuildingClosed : Event
    data class BuildingDeleted(val building: String) : Event
    data class BuildingCreated(val building: String) : Event

    data class AssetDirectorySelected(val dir: File) : Event

    data class ComponentOverrideEnabled(
        val referenceEntity: Entity,
        val templateComponent: Component
    ) : Event

    data class ComponentOverrideDisabled(
        val referenceEntity: Entity,
        val template: EntityTemplate,
        val componentOverride: Component
    ) : Event

    data class PropertyUpdated<T : Any>(
        val component: T,
        val property: KMutableProperty1<out T, *>,
        val newValue: Any
    ) : Event

    data class ViewData(
        val selectedEntity: Entity? = null,
        val selectedEntityData: EntityData? = null,

        val selectedLayer: Int,
        val selectedTool: ToolSelection,
        val hideWalls: Boolean,
        val hideUpperLayers: Boolean,
        val highlightSelectedLayer: Boolean,
        val buildings: Set<String>,
        val selectedBuilding: String?,
        val messageBarText: String,
        val selectedAssetDirectory: File? = null,
        val entityTemplatesInDirectory: Array<EntityTemplate>
    )
}
