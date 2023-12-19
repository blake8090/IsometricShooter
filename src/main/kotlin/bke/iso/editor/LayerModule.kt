package bke.iso.editor

import bke.iso.editor.event.EditorEvent
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module

class IncreaseLayerEvent : EditorEvent()

class DecreaseLayerEvent : EditorEvent()

class LayerModule(private val editorScreen: EditorScreen) : Module {

    var selectedLayer: Int = 0
        private set

    override fun update(deltaTime: Float) {
    }

    override fun handleEvent(event: Event) {
        if (event is IncreaseLayerEvent) {
            selectedLayer++
            editorScreen.updateLayerLabel(selectedLayer.toFloat())
        } else if (event is DecreaseLayerEvent) {
            selectedLayer--
            editorScreen.updateLayerLabel(selectedLayer.toFloat())
        }
    }

    fun init() {
        editorScreen.updateLayerLabel(selectedLayer.toFloat())
    }
}
