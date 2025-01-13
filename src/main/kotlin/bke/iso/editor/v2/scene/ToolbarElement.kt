package bke.iso.editor.v2.scene

import bke.iso.engine.ui.UIElement
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table

class ToolbarElement(skin: Skin) : UIElement(skin) {

    private val root = Table()

    override fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Toolbar", skin, "light"))
            .left()
            .expandX()

        return root
    }
}
