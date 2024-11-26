package bke.iso.editor.scene

import bke.iso.editor.scene.layer.LayerModule
import bke.iso.editor.scene.ui.SceneTabView
import bke.iso.engine.Event
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.Renderer
import bke.iso.engine.state.Module
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Actors
import com.badlogic.gdx.graphics.Color
import io.github.oshai.kotlinlogging.KotlinLogging

class BuildingsModule(
    private val world: World,
    private val renderer: Renderer,
    private val sceneTabView: SceneTabView,
    private val layerModule: LayerModule,
    assets: Assets
) : Module {

    private val log = KotlinLogging.logger {}

    private val buildingFont = assets.fonts[FontOptions("roboto.ttf", 12f, Color.WHITE)]
    private var selectedBuilding: String? = null

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        when (event) {
            is Actors.CreatedEvent -> addActor(event.actor)
        }
    }

    fun draw() {
        for (buildingName in world.buildings.getAll()) {
            drawBuilding(buildingName)
        }
    }

    private fun drawBuilding(name: String) {
        val box = world.buildings.getBounds(name) ?: return

        if (box.min.z > layerModule.selectedLayer && layerModule.hideUpperLayers) {
            return
        }

        val color =
            if (selectedBuilding == name) {
                Color.WHITE
            } else {
                Color.BLUE
            }

        renderer.fgShapes.addBox(box, 1f, color)
        renderer.drawText(name, buildingFont, box.pos)
    }

    private fun addActor(actor: Actor) {
        val building = selectedBuilding ?: return
        world.buildings.add(actor, building)
        log.debug { "Added actor $actor to building '$building'" }
    }

    fun selectBuilding(name: String) {
        selectedBuilding = name
        sceneTabView.updateInfoLabel("Editing building '$name'")
    }

    fun closeBuilding() {
        sceneTabView.updateInfoLabel("")
        selectedBuilding = null
    }
}
