package bke.iso.editor.scene

import bke.iso.editor.EditorEvent
import bke.iso.editor.scene.tool.PointerDeselectActorEvent
import bke.iso.editor.scene.tool.PointerSelectActorEvent
import bke.iso.editor.scene.ui.SceneInspectorView
import bke.iso.engine.Event
import bke.iso.engine.Events
import bke.iso.engine.state.Module
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Description
import bke.iso.engine.world.actor.Tags
import io.github.oshai.kotlinlogging.KotlinLogging

data class CreateTagEvent(val tag: String) : EditorEvent()

data class DeleteTagEvent(val tag: String) : EditorEvent()

data class ApplyBuildingEvent(val name: String) : EditorEvent()

class SceneInspectorModule(
    private val sceneInspectorView: SceneInspectorView,
    private val world: World,
    private val events: Events
) : Module {

    private val log = KotlinLogging.logger {}

    private var selectedActor: Actor? = null

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        when (event) {
            is PointerSelectActorEvent -> selectActor(event.actor)
            is PointerDeselectActorEvent -> clear()
            is CreateTagEvent -> createTag(event.tag)
            is DeleteTagEvent -> deleteTag(event.tag)
            is SceneLoadedEvent -> updateBuildings()
            is BuildingUpdatedEvent -> updateBuildings()
            is ApplyBuildingEvent -> applyBuilding(event.name)
        }
    }

    private fun selectActor(actor: Actor) {
        selectedActor = actor
        log.debug { "selected $actor" }

        val description = actor
            .get<Description>()
            ?.text
            ?: ""
        sceneInspectorView.updateProperties(
            id = actor.id,
            description = description,
            pos = actor.pos
        )

        loadTags(actor)

        val building = world.buildings.getBuilding(actor) ?: ""
        sceneInspectorView.updateSelectedBuilding(building)
    }

    private fun loadTags(actor: Actor) {
        val tags = actor.get<Tags>()
            ?.tags
            ?: emptyList()

        sceneInspectorView.updateTags(tags)
    }

    private fun clear() {
        selectedActor = null
        sceneInspectorView.clear()
        sceneInspectorView.updateSelectedBuilding("")
    }

    private fun createTag(tag: String) {
        val currentActor = selectedActor ?: return

        // although tiles are represented in the editor as actors, in-game tiles cannot have tags
        if (currentActor.has<TilePrefabReference>()) {
            return
        }

        val tagsList = mutableListOf<String>()
        currentActor.with<Tags> { tags ->
            tagsList.addAll(tags.tags)
        }

        tagsList.add(tag)
        currentActor.add(Tags(tagsList))

        sceneInspectorView.updateTags(tagsList)
    }

    private fun deleteTag(tag: String) {
        val currentActor = selectedActor ?: return

        val tagsList = currentActor
            .get<Tags>()
            ?.tags
            ?.toMutableList()
            ?: return

        tagsList.remove(tag)
        currentActor.add(Tags(tagsList))

        sceneInspectorView.updateTags(tagsList)
    }

    private fun updateBuildings() {
        sceneInspectorView.updateBuildingsList(world.buildings.getAll())
    }

    private fun applyBuilding(name: String) {
        val currentActor = selectedActor ?: return
        events.fire(SetBuildingEvent(currentActor, name))
    }
}
