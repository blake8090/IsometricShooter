package bke.iso.editor.scene

import bke.iso.editor.EditorMode
import bke.iso.editor.EditorModule
import bke.iso.editor.color
import bke.iso.editor.scene.command.AddTagCommand
import bke.iso.editor.scene.command.AssignBuildingCommand
import bke.iso.editor.scene.command.DeleteTagCommand
import bke.iso.editor.scene.tool.ToolLogic
import bke.iso.editor.scene.tool.ToolSelection
import bke.iso.engine.Engine
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.asset.prefab.EntityPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Event
import bke.iso.engine.imGuiWantsToCaptureInput
import bke.iso.engine.input.ButtonState
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.scene.EntityRecord
import bke.iso.engine.scene.Scene
import bke.iso.engine.scene.TileRecord
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Entities
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Tags
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.encodeToString

class SceneMode(private val engine: Engine) : EditorMode() {

    override val world = engine.world
    override val renderer = engine.renderer

    private val log = KotlinLogging.logger { }

    var selectedLayer = 0
        private set

    private var gridWidth = 20
    private var gridLength = 20
    private var drawGridForeground = false

    private val cameraLogic = CameraLogic(engine.input, world, renderer, this)
    private val worldLogic = WorldLogic(world, engine.assets, engine.events)
    private val toolLogic =
        ToolLogic(this, engine.input, engine.collisions, engine.renderer, engine.events, world, worldLogic)

    private val view = SceneModeView(engine.assets, engine.events)

    private var hideWalls = false
    var hideUpperLayers = false
        private set
    private var highlightSelectedLayer = false

    private var selectedEntity: Entity? = null
    private var selectedBuilding: String? = null

    private val buildingFont = engine.assets.fonts[FontOptions("roboto.ttf", 12f, Color.WHITE)]

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
        // TODO: set a var instead of passing this - can make everything private
        renderer.occlusion.addStrategy(UpperLayerOcclusionStrategy(this))
    }

    override fun stop() {
        engine.ui.removeImGuiView(view)
        cameraLogic.stop()
    }

    override fun update() {
        cameraLogic.update()

        drawGrid()
        drawSelectedActor()

        for (buildingName in world.buildings.getAll()) {
            drawBuilding(buildingName)
        }

        engine.world.entities.each<Tags>(::drawActorTags)

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
                undo()
            }
            engine.input.onAction("sceneModeRedo") {
                redo()
            }
        }

        view.viewData =
            ViewData(
                selectedEntity = selectedEntity,
                selectedTool = toolLogic.selection,
                selectedLayer = selectedLayer,
                hideWalls = hideWalls,
                hideUpperLayers = hideUpperLayers,
                highlightSelectedLayer = highlightSelectedLayer,
                buildings = engine.world.buildings.getAll(),
                selectedBuilding = selectedBuilding,
                messageBarText = getMessageBarText()
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

    private fun drawSelectedActor() {
        val actor = selectedEntity ?: return
        val collisionBox = actor.getCollisionBox() ?: Box(actor.pos, Vector3(1f, 1f, 1f))
        renderer.fgShapes.addBox(collisionBox, 1f, Color.RED)
    }

    private fun drawActorTags(entity: Entity, tags: Tags) {
        if (tags.tags.isEmpty() || selectedEntity == entity) {
            return
        }
        entity.getCollisionBox()?.let { box ->
            engine.renderer.fgShapes.addBox(box, 1f, color(46, 125, 50))
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
            is SceneLoaded -> resetCommands()

            is ToolSelected -> toolLogic.selectTool(event.selection)
            is TilePrefabSelected -> toolLogic.onTilePrefabSelected(event.prefab)
            is EntityPrefabSelected -> toolLogic.onActorPrefabSelected(event.prefab)
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

            is Entities.CreatedEvent -> {
                worldLogic.setBuilding(event.entity, selectedBuilding)
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

        val actors = mutableListOf<EntityRecord>()
        world.entities.each<EntityPrefabReference> { actor, reference ->
            actors.add(createActorRecord(actor, reference))
        }

        val tiles = mutableListOf<TileRecord>()
        world.entities.each { entity: Entity, reference: TilePrefabReference ->
            tiles.add(
                TileRecord(
                    Location(entity.pos),
                    reference.prefab,
                    world.buildings.getBuilding(entity)
                )
            )
        }

        val scene = Scene("1", actors, tiles)
        val content = engine.serializer.format.encodeToString(scene)
        file.writeText(content)
        log.info { "Saved scene: '${file.canonicalPath}'" }
    }

    private fun createActorRecord(entity: Entity, reference: EntityPrefabReference): EntityRecord {
        val componentOverrides = mutableListOf<Component>()
        entity.with<Tags>(componentOverrides::add)

        return EntityRecord(
            entity.pos,
            reference.prefab,
            world.buildings.getBuilding(entity),
            componentOverrides
        )
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

    class OpenSceneClicked : Event
    class SaveSceneClicked : Event
    class SceneLoaded : Event

    data class ToolSelected(val selection: ToolSelection) : Event
    data class TilePrefabSelected(val prefab: TilePrefab) : Event
    data class EntityPrefabSelected(val prefab: EntityPrefab) : Event
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

    data class ViewData(
        val selectedEntity: Entity? = null,
        val selectedLayer: Int,
        val selectedTool: ToolSelection,
        val hideWalls: Boolean,
        val hideUpperLayers: Boolean,
        val highlightSelectedLayer: Boolean,
        val buildings: Set<String>,
        val selectedBuilding: String?,
        val messageBarText: String,
    )
}
