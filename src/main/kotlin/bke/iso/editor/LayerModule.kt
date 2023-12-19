package bke.iso.editor

import bke.iso.editor.event.EditorEvent
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.Actor
import bke.iso.engine.world.World

class ToggleUpperLayersHiddenEvent : EditorEvent()

class IncreaseLayerEvent : EditorEvent()

class DecreaseLayerEvent : EditorEvent()

class LayerModule(
    private val editorScreen: EditorScreen,
    private val world: World
) : Module {

    var selectedLayer: Int = 0
        private set

    private var hideUpperLayers = false

    override fun update(deltaTime: Float) {
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is IncreaseLayerEvent -> {
                selectedLayer++
                editorScreen.updateLayerLabel(selectedLayer.toFloat())
            }

            is DecreaseLayerEvent -> {
                selectedLayer--
                editorScreen.updateLayerLabel(selectedLayer.toFloat())
            }

            is ToggleUpperLayersHiddenEvent -> {
                hideUpperLayers = !hideUpperLayers
                if (hideUpperLayers) {
                    hideUpperLayers()
                } else {
                    showUpperLayers()
                }
            }
        }
    }

    private fun hideUpperLayers() {
        world.actors.each { actor: Actor, sprite: Sprite ->
            if (actor.z > selectedLayer) {
                sprite.alpha = 0f
            }
        }
    }

    private fun showUpperLayers() {
        world.actors.each { actor: Actor, sprite: Sprite ->
            sprite.alpha = 1f
        }
    }

    fun init() {
        editorScreen.updateLayerLabel(selectedLayer.toFloat())
    }
}
