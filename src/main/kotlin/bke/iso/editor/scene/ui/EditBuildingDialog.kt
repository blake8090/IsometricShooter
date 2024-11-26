package bke.iso.editor.scene.ui

import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class EditBuildingDialog(private val skin: Skin) {

    fun create(
        stage: Stage,
        buildingNames: Set<String>,
        action: (String) -> Unit
    ) {
        val dialog = Dialog("", skin)
        dialog.text("Select a building to edit:")
        dialog.contentTable.row()

        for (buildingName in buildingNames) {
            val button = TextButton(buildingName, skin)
            button.onChanged {
                action.invoke(buildingName)
                dialog.hide()
            }

            dialog.contentTable.row()
            dialog.contentTable
                .add(button)
                .grow()
        }

        dialog
            .button("Cancel", false)
            .key(Input.Keys.ENTER, true)
            .key(Input.Keys.NUMPAD_ENTER, true)
            .key(Input.Keys.ESCAPE, false)

        dialog.show(stage)
    }
}
