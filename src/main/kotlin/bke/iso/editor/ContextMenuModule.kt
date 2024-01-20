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

class ContextMenuModule(
    private val editorScreen: EditorScreen,
    private val buildingsModule: BuildingsModule
) : Module {

    override fun update(deltaTime: Float) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            openContextMenu()
        } else if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
//            editorScreen.closeContextMenu()
        }
    }

    private fun openContextMenu() {
        if (editorScreen.hitMainView()) {
            openMainViewContextMenu()
        }
    }

    private fun openMainViewContextMenu() {
        val selections = mutableSetOf<ContextMenuSelection>()

        if (buildingsModule.selectedBuilding.isNullOrBlank()) {
            selections.add(ContextMenuSelection("New building") {
                buildingsModule.newBuilding()
            })
        } else {
            selections.add(ContextMenuSelection("Close building") {
                buildingsModule.closeBuilding()
            })
        }

        if (selections.isNotEmpty()) {
            editorScreen.openContextMenu(selections)
        }
    }

    override fun handleEvent(event: Event) {}
}
