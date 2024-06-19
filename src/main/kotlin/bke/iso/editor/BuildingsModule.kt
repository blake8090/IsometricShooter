package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import com.badlogic.gdx.graphics.Color
import mu.KotlinLogging

class BuildingsModule(
    private val world: World,
    private val renderer: Renderer,
    private val editorScreen: EditorScreen,
    assets: Assets
) : Module {

    private val log = KotlinLogging.logger {}

    private val buildingFont = assets.fonts[FontOptions("roboto.ttf", 12f, Color.WHITE)]

    var selectedBuilding: String? = null
        private set

    fun init() {
    }

    override fun update(deltaTime: Float) {
        for (buildingName in world.buildings.getAll()) {
            drawBuilding(buildingName)
        }
    }

    private fun drawBuilding(name: String) {
        val box = world.buildings.getBounds(name) ?: return

        val color =
            if (selectedBuilding == name) {
                Color.WHITE
            } else {
                Color.BLUE
            }

        renderer.fgShapes.addBox(box, 1f, color)
        renderer.drawText(name, buildingFont, box.pos)
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
