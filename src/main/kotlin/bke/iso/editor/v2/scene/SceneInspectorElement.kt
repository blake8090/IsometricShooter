package bke.iso.editor.v2.scene

import bke.iso.editor.ui.color
import bke.iso.engine.ui.UIElement
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table

class SceneInspectorElement(skin: Skin) : UIElement(skin) {

    private val root = Table()
    private val content = BorderedTable(color(43, 103, 161)).apply {
        top()
        left()
    }

    override fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Inspector", skin, "light"))
            .left()

        root.row()
        root.add(content).grow()

        return root
    }
}
