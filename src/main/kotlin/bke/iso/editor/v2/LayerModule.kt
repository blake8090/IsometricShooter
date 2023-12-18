package bke.iso.editor.v2

import bke.iso.editor.event.DecreaseLayerEvent
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.event.EditorEventWrapper
import bke.iso.editor.event.IncreaseLayerEvent
import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module

class LayerModule(private val editorScreen: EditorScreen) : Module {

    var selectedLayer: Int = 0
        private set

    override fun update(deltaTime: Float) {
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is EditorEventWrapper -> {
                handleEvent(event.editorEvent)
            }
        }
    }

    private fun handleEvent(editorEvent: EditorEvent) {
        when (editorEvent) {
            is IncreaseLayerEvent -> {
                selectedLayer++
                editorScreen.updateLayerLabel(selectedLayer.toFloat())
            }

            is DecreaseLayerEvent -> {
                selectedLayer--
                editorScreen.updateLayerLabel(selectedLayer.toFloat())
            }
        }
    }
}
