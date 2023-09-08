package bke.iso.editor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class EditorMenuBar(private val skin: Skin) {

    fun create(): Table {
        setup()

        val menuBar = Table().left()
        menuBar.background = skin.newDrawable("pixel", Color.DARK_GRAY)

        val vPad = .25f
        val hPad = .18f

        val newButton = textButton("New", skin, "menu").apply {
            padTop(Value.percentHeight(vPad, this))
            padBottom(Value.percentHeight(vPad, this))
            padLeft(Value.percentWidth(hPad, this))
            padRight(Value.percentWidth(hPad, this))
        }
        menuBar.add(newButton)

        val openButton = textButton("Open", skin, "menu").apply {
            padTop(Value.percentHeight(vPad, this))
            padBottom(Value.percentHeight(vPad, this))
            padLeft(Value.percentWidth(hPad, this))
            padRight(Value.percentWidth(hPad, this))
        }
        menuBar.add(openButton)

        val saveButton = textButton("Save", skin, "menu").apply {
            padTop(Value.percentHeight(vPad, this))
            padBottom(Value.percentHeight(vPad, this))
            padLeft(Value.percentWidth(hPad, this))
            padRight(Value.percentWidth(hPad, this))
        }
        menuBar.add(saveButton)

        return menuBar
    }

    private fun setup() {
        skin.add("menu", TextButton.TextButtonStyle().apply {
            font = skin.getFont("font")
            up = skin.newDrawable("pixel", Color.DARK_GRAY)
            down = skin.newDrawable("pixel", Color.GRAY)
            over = skin.newDrawable("pixel", color(94, 94, 94))
        })
    }
}
