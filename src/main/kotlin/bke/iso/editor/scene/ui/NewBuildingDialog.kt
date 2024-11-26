package bke.iso.editor.scene.ui

import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField

class NewBuildingDialog(private val skin: Skin) {

    fun create(stage: Stage, action: (String) -> Unit) {

        val textField = TextField("", skin)

        val dialog = object : Dialog("", skin) {

            override fun result(obj: Any) {
                val result = obj as Boolean
                val text = textField.text

                if (result && text.isNotBlank()) {
                    action.invoke(textField.text)
                }
            }
        }

        dialog.text("Enter building name:")
        dialog.contentTable.row()
        dialog.contentTable
            .add(textField)
            .padLeft(5f)
            .padRight(5f)

        dialog
            .button("OK", true)
            .button("Cancel", false)
            .key(Input.Keys.ENTER, true)
            .key(Input.Keys.NUMPAD_ENTER, true)
            .key(Input.Keys.ESCAPE, false)

        dialog.show(stage)
        stage.keyboardFocus = textField
    }
}
