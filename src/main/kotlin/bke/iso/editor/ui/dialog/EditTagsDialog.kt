package bke.iso.editor.ui.dialog

import bke.iso.editor.ui.color
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Tags
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.Window
import io.github.oshai.kotlinlogging.KotlinLogging

private const val STYLE_NAME = "edit-tags-dialog"

class EditTagsDialog(private val skin: Skin) {

    private val log = KotlinLogging.logger {}

    private val pendingTags = mutableListOf<String>()

    fun create(stage: Stage, actor: Actor) {
        setup()

        pendingTags.clear()

        val dialog = object : Dialog("", skin, STYLE_NAME) {
            override fun result(obj: Any) {
                val result = obj as Boolean

                if (result) {
                    actor.add(Tags(pendingTags.toList()))
                    log.debug { "Set tags: ${pendingTags.joinToString()}" }
                } else {
                    log.debug { "User cancelled edit tags" }
                }
            }
        }

        dialog.contentTable
            .add(Label("Edit Tags", skin))
            .left()
            .padLeft(5f)
            .row()

        val tagTable = BorderedTable(color(77, 100, 130))
            .top()
            .left()

        actor.with<Tags> { tags ->
            for (tag in tags.tags) {
                addTag(tag, tagTable)
            }
        }

        val scrollPane = ScrollPane(tagTable, skin, STYLE_NAME)
        scrollPane.fadeScrollBars = false

        dialog.contentTable
            .add(scrollPane)
            .colspan(2)
            .width(300f)
            .height(200f)

        dialog.contentTable.row()

        val textField = TextField("", skin, STYLE_NAME)
        dialog.contentTable.add(textField)

        val button = TextButton("Add", skin)
        button.onChanged {
            addTag(textField.text, tagTable)
        }
        dialog.contentTable.add(button)

        dialog
            .button("OK", true)
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

            add(STYLE_NAME, TextButton.TextButtonStyle().apply {
                font = skin.getFont("default")
                up = skin.newTintedDrawable("pixel", "button-up")
                down = skin.newTintedDrawable("pixel", "button-down")
                over = skin.newTintedDrawable("pixel", "button-over")
                checked = skin.newTintedDrawable("pixel", "button-checked")
            })

            add(STYLE_NAME, ScrollPane.ScrollPaneStyle().apply {
                vScroll = skin.newTintedDrawable("pixel", "button-up")
                vScrollKnob = skin.newTintedDrawable("pixel", "button-down")
                vScroll.minWidth = 10f
                vScrollKnob.minWidth = 10f

                hScroll = skin.newTintedDrawable("pixel", "button-up")
                hScrollKnob = skin.newTintedDrawable("pixel", "button-down")
                hScroll.minHeight = 10f
                hScrollKnob.minHeight = 10f
            })
        }
    }

    private fun addTag(tag: String, tagTable: Table) {
        if (tag.isBlank()) {
            return
        }

        tagTable.row()

        val label = Label(tag, skin)
        label.style = Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.newTintedDrawable("pixel", "button-up")
        }

        tagTable.add(label)
            .growX()
            .padLeft(4f)
            .padRight(4f)
            .padTop(2f)
            .padBottom(2f)


        val button = TextButton("Delete", skin)
        button.onChanged {
            tagTable.removeActor(label)
            tagTable.removeActor(button)
            pendingTags.remove(tag)
            log.debug { "Removed tag '$tag'" }
        }

        tagTable.add(button)
            .padRight(4f)

        pendingTags.add(tag)

        log.debug { "Added tag '$tag'" }
    }
}
