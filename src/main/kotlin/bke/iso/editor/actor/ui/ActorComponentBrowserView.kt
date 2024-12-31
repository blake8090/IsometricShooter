package bke.iso.editor.actor.ui

import bke.iso.editor.actor.OpenAddComponentDialogEvent
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

    private val root = Table().apply {
        top()
        left()
    }

    private lateinit var componentList: Table

    fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Components", skin))

        root.row()

        root.add(createAddComponentButton(skin))
            .left()

        componentList = BorderedTable(skin.getColor("table-border"))

        root.row()
        root.add(componentList)
            .growX()

        return root
    }

    private fun createAddComponentButton(skin: Skin): TextButton =
        TextButton("Add", skin).apply {
            onChanged {
                fire(OpenAddComponentDialogEvent())
            }
        }

    fun updateComponents(components: List<Component>) {
        componentList.clearChildren()

        val buttonGroup = ButtonGroup<TextButton>()
        for (component in components) {
            val textButton = createTextButton(component)
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

    private fun createTextButton(component: Component): TextButton =
        TextButton(component::class.simpleName, skin, "checkable").apply {
            label.setAlignment(Align.left)
            onChanged {
                if (isChecked) {
                    fire(SelectComponentEvent(component))
                }
            }
        }
}
