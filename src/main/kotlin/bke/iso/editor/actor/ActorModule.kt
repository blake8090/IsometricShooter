package bke.iso.editor.actor

import bke.iso.editor.EditorEvent
import bke.iso.editor.actor.ui.ActorComponentBrowserView
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.math.Box
import bke.iso.engine.os.Dialogs
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class OpenActorEvent : EditorEvent()

class SelectComponentEvent(val component: Component) : EditorEvent()

class ActorModule(
    private val dialogs: Dialogs,
    private val serializer: Serializer,
    private val world: World,
    private val actorTabRenderer: Renderer,
    private val actorComponentBrowserView: ActorComponentBrowserView
) : Module {

    private val log = KotlinLogging.logger {}

    private lateinit var selectedPrefab: ActorPrefab
    private lateinit var referenceActor: Actor

    override fun start() {
        referenceActor = world.actors.create(Vector3())
        loadPrefab(ActorPrefab("", mutableListOf()))
        log.debug { "started actor tab" }
    }

    override fun handleEvent(event: Event) {
        if (event is OpenActorEvent) {
            loadActorPrefab()
        }
    }

    override fun update(deltaTime: Float) {
        actorTabRenderer.fgShapes.addPoint(referenceActor.pos, 2f, Color.RED)
        drawPrefab()
    }

    private fun drawPrefab() {
        selectedPrefab.components.withFirstInstance<Collider> { collider ->
            val min = referenceActor.pos.add(collider.offset)
            val max = Vector3(min).add(collider.size)
            val box = Box.fromMinMax(min, max)
            actorTabRenderer.fgShapes.addBox(box, 1f, Color.CYAN)
            actorTabRenderer.fgShapes.addPoint(box.pos, 2f, Color.CYAN)
        }
    }

    private fun loadActorPrefab() {
        val file = dialogs.showOpenFileDialog("Actor Prefab", "actor") ?: return
        val prefab = serializer.read<ActorPrefab>(file.readText())
        loadPrefab(prefab)
        log.info { "Loaded actor prefab: '${file.canonicalPath}'" }
    }

    private fun loadPrefab(prefab: ActorPrefab) {
        selectedPrefab = prefab
        updateComponentsView()
    }

    private fun updateComponentsView() {
        selectedPrefab.components.withFirstInstance<Sprite> { sprite ->
            referenceActor.add(sprite)
        }
        actorComponentBrowserView.updateComponents(selectedPrefab.components)
    }

    fun <T : KClass<out Component>> addNewComponent(type: T) {
        val component = type.createInstance()
        selectedPrefab.components.add(component)
        updateComponentsView()
        log.debug { "Added component type '${type.simpleName}' to prefab" }
    }
}
