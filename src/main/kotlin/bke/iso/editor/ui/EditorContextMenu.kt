package bke.iso.editor.ui

import bke.iso.editor.ContextMenuSelection
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class EditorContextMenu(private val skin: Skin) {

    fun create(x: Float, y: Float, vararg selections: ContextMenuSelection): Actor {
        setup()

        val root = Table().top().left()

        val menu = Table()
        menu.background = skin.getDrawable("bg")

        for (selection in selections) {
            menu.add(TextButton(selection.text, skin, "context-menu"))
                .pad(5f)
            menu.row()
        }

        root.add(menu).expand()
        root.setPosition(x, y)

        return root
    }

    private fun setup() {
        skin.add("context-menu", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.newTintedDrawable("pixel", "button-up")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
        })
    }
}
