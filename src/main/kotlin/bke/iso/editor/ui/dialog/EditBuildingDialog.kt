package bke.iso.editor.ui.dialog

import bke.iso.editor.ui.color
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Window
import mu.KotlinLogging

private const val STYLE_NAME = "edit-building-dialog"

class EditBuildingDialog(private val skin: Skin) {

    private val log = KotlinLogging.logger {}

    fun create(
        stage: Stage,
        buildingNames: Set<String>,
        action: (String) -> Unit
    ) {
        setup()

        val dialog = Dialog("", skin, STYLE_NAME)
        dialog.text("Select a building to edit:")
        dialog.contentTable.row()

        for (buildingName in buildingNames) {
            val button = TextButton(buildingName, skin)
            button.onChanged {
                log.debug { "Selected building: '$buildingName'" }
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

            add(STYLE_NAME, TextButtonStyle().apply {
                font = skin.getFont("default")
                up = skin.newTintedDrawable("pixel", "button-up")
                down = skin.newTintedDrawable("pixel", "button-down")
                over = skin.newTintedDrawable("pixel", "button-over")
                checked = skin.newTintedDrawable("pixel", "button-checked")
            })
        }
    }
}
