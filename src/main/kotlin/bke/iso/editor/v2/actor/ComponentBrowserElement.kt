package bke.iso.editor.v2.actor

import bke.iso.engine.ui.UIElement
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

class ComponentBrowserElement(skin: Skin) : UIElement(skin) {

    private val root = Table().apply {
        top()
        left()
    }

    private lateinit var componentList: Table
    private val buttonGroup = ButtonGroup<TextButton>()

    override fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Components", skin))

        root.row()

        root.add(createAddComponentButton(skin))
            .left()

        root.add(createDeleteComponentButton(skin))
            .left()
            .expandX()

        componentList = BorderedTable(skin.getColor("table-border"))

        root.row()
        root.add(componentList)
            .colspan(2)
            .growX()

        return root
    }

    private fun createAddComponentButton(skin: Skin): TextButton =
        TextButton("Add", skin).apply {
            onChanged {
                fire(ActorTabViewController.OpenSelectNewComponentDialogEvent())
            }
        }

    private fun createDeleteComponentButton(skin: Skin): TextButton =
        TextButton("Delete", skin).apply {
            onChanged {
                val checkedButton = this@ComponentBrowserElement.buttonGroup.checked
                if (checkedButton != null) {
                    val index = this@ComponentBrowserElement.buttonGroup.checkedIndex
                    fire(ActorTabViewController.DeleteComponentEvent(index))
                }
            }
        }

    fun update(components: List<Component>) {
        componentList.clearChildren()
        buttonGroup.clear()
        buttonGroup

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
                    fire(ActorTabViewController.SelectComponentEvent(component))
                }
            }
        }
}
