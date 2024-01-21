package bke.iso.editor.ui.dialog

import bke.iso.editor.ui.color
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Window
import mu.KotlinLogging

private const val STYLE_NAME = "new-building-dialog"

class NewBuildingDialog(private val skin: Skin) {

    private val log = KotlinLogging.logger {}

    fun create(stage: Stage, action: (String) -> Unit) {
        setup()

        val textField = TextField("", skin, STYLE_NAME)

        val dialog = object : Dialog("", skin, STYLE_NAME) {

            override fun result(obj: Any) {
                val result = obj as Boolean
                val text = textField.text

                if (result && text.isNotBlank()) {
                    log.debug { "Result: '$text'" }
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

    private fun setup() {
        skin.apply {
            add(STYLE_NAME, TextField.TextFieldStyle().apply {
                font = skin.getFont("default")
                fontColor = Color.WHITE
                focusedFontColor = Color.WHITE

                background = skin.newDrawable("pixel", Color.BLACK)
                focusedBackground = skin.newDrawable("pixel", Color.BLACK)

                cursor = skin.newDrawable("pixel", color(50, 158, 168))
                selection = skin.newDrawable("pixel", color(50, 158, 168))
            })

            add(STYLE_NAME, Window.WindowStyle().apply {
                background = skin.getDrawable("bg")
                titleFont = skin.getFont("default")
            })
        }
    }
}