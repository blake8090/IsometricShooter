package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.state.Module
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2

interface ContextMenuSelection

data class DefaultContextMenuSelection(
    val text: String,
    val action: () -> Unit
) : ContextMenuSelection

data class CheckableContextMenuSelection(
    val text: String,
    val action: () -> Unit,
    val isChecked: () -> Boolean
) : ContextMenuSelection

data class OpenContextMenuEvent(
    val pos: Vector2,
    val selections: Set<ContextMenuSelection>
) : EditorEvent()

class CloseContextMenuEvent : EditorEvent()

class ContextMenuModule(private val editorScreen: EditorScreen) : Module {

    override fun update(deltaTime: Float) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (!editorScreen.touchedContextMenu()) {
                editorScreen.closeContextMenu()
            }
        }
    }

    override fun handleEvent(event: Event) {
        if (event is OpenContextMenuEvent) {
            openContextMenu(event)
        } else if (event is CloseContextMenuEvent) {
            editorScreen.closeContextMenu()
        }
    }

    private fun openContextMenu(event: OpenContextMenuEvent) {
        editorScreen.openContextMenu(event.pos, event.selections)
    }
}
