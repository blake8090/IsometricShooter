package bke.iso.editor2.scene

import bke.iso.editor.scene.ActorPrefabReference
import bke.iso.editor.scene.TilePrefabReference
import bke.iso.editor.ui.color
import bke.iso.editor2.EditorMode
import bke.iso.editor2.ImGuiEditorState
import bke.iso.editor2.scene.command.AddTagCommand
import bke.iso.editor2.scene.command.AssignBuildingCommand
import bke.iso.editor2.scene.command.DeleteTagCommand
import bke.iso.editor2.scene.tool.ToolLogic
import bke.iso.editor2.scene.tool.ToolSelection
import bke.iso.engine.Engine
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.core.Event
import bke.iso.engine.imGuiWantsToCaptureInput
import bke.iso.engine.input.ButtonState
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.scene.ActorRecord
import bke.iso.engine.scene.Scene
import bke.iso.engine.scene.TileRecord
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.actor.Tags
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
    private val worldLogic = WorldLogic(world, engine.assets, engine.events, engine.dialogs, engine.serializer)
    private val toolLogic =
        ToolLogic(this, engine.input, engine.collisions, engine.renderer, engine.events, world, worldLogic)
    private val view = SceneModeView(engine.assets, engine.events)

    private var hideWalls = false
    var hideUpperLayers = false
        private set
    private var highlightSelectedLayer = false

    private var selectedActor: Actor? = null
    private var selectedBuilding: String? = null

    private val buildingFont = engine.assets.fonts[FontOptions("roboto.ttf", 12f, Color.WHITE)]

    override fun start() {
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

        renderer.occlusion.resetStrategies()
        // TODO: set a var instead of passing this - can make everything private
        renderer.occlusion.addStrategy(UpperLayerOcclusionStrategy(this))
    }

    override fun stop() {
        cameraLogic.stop()
    }

    override fun update() {
        cameraLogic.update()

        drawGrid()
        drawSelectedActor()

        for (buildingName in world.buildings.getAll()) {
            drawBuilding(buildingName)
        }

        engine.world.actors.each<Tags>(::drawActorTags)

        toolLogic.update()

        if (!imGuiWantsToCaptureInput()) {
            engine.input.onAction("sceneTabToolPress") {
                toolLogic.performAction()?.let { command ->
                    engine.events.fire(ImGuiEditorState.ExecuteCommand(command))
                }
            }

            engine.input.onAction("sceneTabToolDown") {
                toolLogic.performMultiAction()?.let { command ->
                    engine.events.fire(ImGuiEditorState.ExecuteCommand(command))
                }
            }

            engine.input.onAction("sceneTabToolRelease") {
                toolLogic.performReleaseAction()?.let { command ->
                    engine.events.fire(ImGuiEditorState.ExecuteCommand(command))
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
        val actor = selectedActor ?: return
        val collisionBox = actor.getCollisionBox() ?: Box(actor.pos, Vector3(1f, 1f, 1f))
        renderer.fgShapes.addBox(collisionBox, 1f, Color.RED)
    }

    private fun drawActorTags(actor: Actor, tags: Tags) {
        if (tags.tags.isEmpty() || selectedActor == actor) {
            return
        }
        actor.getCollisionBox()?.let { box ->
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

    override fun draw() {
        view.draw(
            ViewData(
                selectedActor = selectedActor,
                selectedTool = toolLogic.selection,
                selectedLayer = selectedLayer,
                hideWalls = hideWalls,
                hideUpperLayers = hideUpperLayers,
                highlightSelectedLayer = highlightSelectedLayer,
                buildings = engine.world.buildings.getAll(),
                selectedBuilding = getSelectedBuilding()
            )
        )
    }

    private fun getSelectedBuilding(): String? {
        val actor = selectedActor ?: return null
        return engine.world.buildings.getBuilding(actor)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is OpenSceneClicked -> openScene()
            is SaveSceneClicked -> saveScene()
            is SceneLoaded -> resetCommands()

            is ToolSelected -> toolLogic.selectTool(event.selection)
            is TilePrefabSelected -> toolLogic.onTilePrefabSelected(event.prefab)
            is ActorPrefabSelected -> toolLogic.onActorPrefabSelected(event.prefab)
            is ActorSelected -> selectedActor = event.actor

            is LayerIncreased -> selectedLayer++
            is LayerDecreased -> selectedLayer--

            is HideWallsToggled -> toggleHideWalls()
            is HideUpperLayersToggled -> hideUpperLayers = !hideUpperLayers
            is HighlightSelectedLayerToggled -> highlightSelectedLayer = !highlightSelectedLayer

            is TagAdded -> {
                val command = AddTagCommand(event.actor, event.tag)
                engine.events.fire(ImGuiEditorState.ExecuteCommand(command))
            }

            is TagDeleted -> {
                val command = DeleteTagCommand(event.actor, event.tag)
                engine.events.fire(ImGuiEditorState.ExecuteCommand(command))
            }

            is BuildingAssigned -> {
                val command = AssignBuildingCommand(event.actor, event.building, worldLogic)
                engine.events.fire(ImGuiEditorState.ExecuteCommand(command))
            }
        }
    }

    private fun openScene() {
        worldLogic.loadScene()
    }

    private fun saveScene() {
        val file = engine.dialogs.showSaveFileDialog("Scene", "scene") ?: return

        val actors = mutableListOf<ActorRecord>()
        world.actors.each<ActorPrefabReference> { actor, reference ->
            actors.add(createActorRecord(actor, reference))
        }

        val tiles = mutableListOf<TileRecord>()
        world.actors.each { actor: Actor, reference: TilePrefabReference ->
            tiles.add(
                TileRecord(
                    Location(actor.pos),
                    reference.prefab,
                    world.buildings.getBuilding(actor)
                )
            )
        }

        val scene = Scene("1", actors, tiles)
        val content = engine.serializer.format.encodeToString(scene)
        file.writeText(content)
        log.info { "Saved scene: '${file.canonicalPath}'" }
    }

    private fun createActorRecord(actor: Actor, reference: ActorPrefabReference): ActorRecord {
        val componentOverrides = mutableListOf<Component>()
        actor.with<Tags>(componentOverrides::add)

        return ActorRecord(
            actor.pos,
            reference.prefab,
            world.buildings.getBuilding(actor),
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
    data class ActorPrefabSelected(val prefab: ActorPrefab) : Event
    data class ActorSelected(val actor: Actor) : Event

    class LayerIncreased : Event
    class LayerDecreased : Event

    class HideWallsToggled : Event
    class HideUpperLayersToggled : Event
    class HighlightSelectedLayerToggled : Event

    class TagAdded(val actor: Actor, val tag: String) : Event
    data class TagDeleted(val actor: Actor, val tag: String) : Event

    data class BuildingAssigned(val actor: Actor, val building: String?) : Event

    data class ViewData(
        val selectedActor: Actor? = null,
        val selectedLayer: Int,
        val selectedTool: ToolSelection,
        val hideWalls: Boolean,
        val hideUpperLayers: Boolean,
        val highlightSelectedLayer: Boolean,
        val buildings: Set<String>,
        val selectedBuilding: String?
    )
}
