package bke.iso.editor.ui

import bke.iso.editor.event.SaveSceneEvent
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class EditorMenuBar(private val skin: Skin) {

    fun create(): Table {
        setup()

        val menuBar = Table().left()
        menuBar.background = skin.getDrawable("bg")

        menuBar.add(createMenuButton("New"))
        menuBar.add(createMenuButton("Open"))

        val saveButton = createMenuButton("Save")
        saveButton.onChanged {
            fire(SaveSceneEvent())
        }
        menuBar.add(saveButton)
        return menuBar
    }

    private fun setup() {
        skin.add("menu", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.getDrawable("bg")
            down = skin.newDrawable("pixel", Color.GRAY)
            over = skin.newDrawable("pixel", color(94, 94, 94))
        })
    }

    private fun createMenuButton(text: String): TextButton {
        val vPad = 10f
        val hPad = 10f
        return TextButton(text, skin, "menu").apply {
            padTop(vPad)
            padBottom(vPad)
            padLeft(hPad)
            padRight(hPad)
        }
    }
}
