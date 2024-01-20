package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import com.badlogic.gdx.graphics.Color
import mu.KotlinLogging

class BuildingsModule(
    private val world: World,
    private val renderer: Renderer,
    private val editorScreen: EditorScreen
) : Module {

    private val log = KotlinLogging.logger {}

    var selectedBuilding: String? = null
        private set

    fun init() {
    }

    override fun update(deltaTime: Float) {
        val boundingBoxes = world
            .buildings
            .getAll()
            .mapNotNull { name -> world.buildings.getBounds(name) }

        for (box in boundingBoxes) {
            renderer.fgShapes.addBox(box, 1f, Color.BLUE)
        }
    }

    override fun handleEvent(event: Event) {
        if (event is Actors.CreatedEvent) {
            addActor(event.actor)
        }
    }

    private fun addActor(actor: Actor) {
        val building = selectedBuilding ?: return
        world.buildings.add(actor, building)
        log.debug { "Added actor $actor to building '$building'" }
    }

    fun selectBuilding(name: String) {
        selectedBuilding = name
        editorScreen.setInfoText("Editing building '$name'")
    }

    fun closeBuilding() {
        editorScreen.setInfoText("")
        selectedBuilding = null
    }
}
