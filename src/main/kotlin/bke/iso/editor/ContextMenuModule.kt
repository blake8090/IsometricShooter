package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

data class ContextMenuSelection(
    val text: String,
    val action: () -> Unit
)

class ContextMenuModule(
    private val editorScreen: EditorScreen,
    private val buildingsModule: BuildingsModule,
    private val world: World
) : Module {

    override fun update(deltaTime: Float) {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            openContextMenu()
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
                editorScreen.openNewBuildingDialog(buildingsModule::selectBuilding)
                editorScreen.closeContextMenu()
            })
        } else {
            selections.add(ContextMenuSelection("Close building") {
                buildingsModule.closeBuilding()
                editorScreen.closeContextMenu()
            })
        }

        selections.add(ContextMenuSelection("Edit building") {
            val buildingNames = world.buildings.getAll()
            editorScreen.openEditBuildingDialog(buildingNames) { name ->
                buildingsModule.selectBuilding(name)
            }
            editorScreen.closeContextMenu()
        })

        selections.add(ContextMenuSelection("Delete building") {
            editorScreen.closeContextMenu()
        })

        editorScreen.openContextMenu(selections)
    }

    override fun handleEvent(event: Event) {}
}
