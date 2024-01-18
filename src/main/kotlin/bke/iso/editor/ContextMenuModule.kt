package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

data class ContextMenuSelection(
    val text: String,
    val action: () -> Unit
)

class ContextMenuModule(private val editorScreen: EditorScreen) : Module {

    override fun update(deltaTime: Float) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            openContextMenu()
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            editorScreen.closeContextMenu()
        }
    }

    private fun openContextMenu() {
        if (editorScreen.hitMainView()) {
            openMainViewContextMenu()
        }
    }

    private fun openMainViewContextMenu() {
        editorScreen.openContextMenu(
            ContextMenuSelection("New Building") {},
            ContextMenuSelection("Edit Building") {}
        )
    }

    override fun handleEvent(event: Event) {
    }
}
