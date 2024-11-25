package bke.iso.editorv2.scene.layer

import bke.iso.editorv2.EditorEvent
import bke.iso.editorv2.PerformActionEvent
import bke.iso.editorv2.scene.ui.SceneTabView
import bke.iso.engine.Event
import bke.iso.engine.Events
import bke.iso.engine.state.Module
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.Sprite
import bke.iso.engine.render.SpriteTintColor
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.World
import kotlin.math.floor

class ToggleUpperLayersHiddenEvent : EditorEvent()

class ToggleHighlightLayerEvent : EditorEvent()

class IncreaseLayerEvent : EditorEvent()

class DecreaseLayerEvent : EditorEvent()

data class ChangeSelectedLayerEvent(val selectedLayer: Float) : Event

class LayerModule2(
    private val sceneTab: SceneTabView,
    private val world: World,
    private val events: Events,
    private val renderer: Renderer
) : Module {

    var selectedLayer: Int = 0
        private set

    var hideUpperLayers = false
        private set
    private var highlightLayer = false

    override fun update(deltaTime: Float) {}

    override fun handleEvent(event: Event) {
        when (event) {
            is IncreaseLayerEvent -> {
                selectedLayer++
                sceneTab.updateLayerLabel(selectedLayer.toFloat())
                highlightActors()
                events.fire(ChangeSelectedLayerEvent(selectedLayer.toFloat()))
            }

            is DecreaseLayerEvent -> {
                selectedLayer--
                sceneTab.updateLayerLabel(selectedLayer.toFloat())
                highlightActors()
                events.fire(ChangeSelectedLayerEvent(selectedLayer.toFloat()))
            }

            is ToggleUpperLayersHiddenEvent -> {
                hideUpperLayers = !hideUpperLayers
            }

            is ToggleHighlightLayerEvent -> {
                highlightLayer = !highlightLayer
                highlightActors()
            }

            is PerformActionEvent -> {
                highlightActors()
            }
        }
    }

    private fun highlightActors() {
        world.actors.each<Sprite> { actor, _ ->
            actor.remove<SpriteTintColor>()
            if (canHighlight(actor)) {
                actor.add(SpriteTintColor(0.8f, 0.1f, 0.1f))
            }
        }
    }

    private fun canHighlight(actor: Actor) =
        highlightLayer && floor(actor.z).toInt() == selectedLayer

    fun init() {
        sceneTab.updateLayerLabel(selectedLayer.toFloat())
        renderer.occlusion.resetStrategies()
        renderer.occlusion.addStrategy(UpperLayerOcclusionStrategy2(this))
    }
}
