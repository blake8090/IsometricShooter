package bke.iso.editor.actor.ui

import bke.iso.editor.actor.SelectComponentEvent
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align

class ActorComponentBrowserView(private val skin: Skin) {

    private val root = Table()
        .top()
        .padLeft(5f)
        .padRight(5f)

    private lateinit var componentList: Table

    fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Components", skin))
            .left()

        root.row()

        root.add(TextButton("Add", skin))
            .padTop(10f)
            .left()

        componentList = BorderedTable(skin.getColor("table-border")).left()

        root.row()
        root.add(componentList)

        return root
    }

    fun updateComponents(components: List<Component>) {
        componentList.clearChildren()

        val buttonGroup = ButtonGroup<TextButton>()
        for (component in components) {
            val textButton = TextButton(component::class.simpleName, skin, "checkable")
            textButton.label.setAlignment(Align.left)
            textButton.onChanged {
                if (isChecked) {
                    fire(SelectComponentEvent(component))
                }
            }

            buttonGroup.add(textButton)
            componentList.add(textButton)
                .left()
                .growX()
                .row()
        }

        buttonGroup.uncheckAll()

        buttonGroup.buttons
            .firstOrNull()
            ?.isChecked = true
    }
}
