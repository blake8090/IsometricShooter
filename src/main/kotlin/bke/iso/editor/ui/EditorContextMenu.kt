package bke.iso.editor.ui

import bke.iso.editor.ContextMenuSelection
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import mu.KotlinLogging

class EditorContextMenu(private val skin: Skin) {

    private val log = KotlinLogging.logger {}

    fun create(x: Float, y: Float, selections: Set<ContextMenuSelection>): Actor {
        setup()

        val root = Table().top().left()

        val menu = Table()
        menu.background = skin.getDrawable("bg")

        // prevents clicks on main view when cursor is over the context menu
        menu.touchable = Touchable.enabled
        menu.addListener(object : ClickListener() {
            override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                log.debug { "touched context menu" }
                return true
            }
        })

        for (selection in selections) {
            val button = TextButton(selection.text, skin, "context-menu")
            button.onChanged {
                selection.action.invoke()
            }

            menu.add(button)
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
