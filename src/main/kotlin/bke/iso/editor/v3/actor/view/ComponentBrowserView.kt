package bke.iso.editor.v3.actor.view

import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.ui.v2.UIView
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align

class ComponentBrowserView(private val skin: Skin) : UIView() {

    private lateinit var componentList: Table
    private val buttonGroup = ButtonGroup<TextButton>()

    override fun create() {
        root.top().left()
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
    }

    private fun createAddComponentButton(skin: Skin): TextButton =
        TextButton("Add", skin).apply {
            onChanged {
                fire(OnAddButtonClicked())
            }
        }

    private fun createDeleteComponentButton(skin: Skin): TextButton =
        TextButton("Delete", skin).apply {
            onChanged {
                fire(OnDeleteButtonClicked())
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
                    fire(OnComponentSelected(component))
                }
            }
        }

    fun getCheckedIndex() =
        buttonGroup.checkedIndex

    class OnAddButtonClicked : Event()

    class OnDeleteButtonClicked : Event()

    class OnComponentSelected(val component: Component) : Event()
}
