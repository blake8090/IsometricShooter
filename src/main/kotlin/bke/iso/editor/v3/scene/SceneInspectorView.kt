package bke.iso.editor.v3.scene

import bke.iso.editor.ui.color
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.ui.v2.UIView
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField

class SceneInspectorView(private val skin: Skin) : UIView() {

    private val content = Table().apply {
        top()
        left()
    }

    // TODO: use by lazy instead?
    private lateinit var idTextField: TextField
    private lateinit var descriptionTextField: TextField
    private lateinit var xPosTextField: TextField
    private lateinit var yPosTextField: TextField
    private lateinit var zPosTextField: TextField

    private lateinit var tagsTable: Table
    private lateinit var buildingSelectBox: SelectBox<String>

    override fun create() {
        root.background = skin.getDrawable("bg")

        root.add(Label("Inspector", skin, "light"))
            .left()

        content.add(createPropertiesSection())
            .growX()

        content.row()
        content.add(createTagsSection())
            .growX()

        content.row()
        content.add(createBuildingSection())
            .growX()

        root.row()
        root.add(content).grow()
    }

    private fun createPropertiesSection(): Table {
        val table = BorderedTable(color(43, 103, 161))
            .padLeft(5f)
            .padRight(5f)

        table.add(Label("id:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        idTextField = TextField("", skin)
        idTextField.isDisabled = true
        table.add(idTextField)
            .growX()

        table.row()
        table.add(Label("description:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        descriptionTextField = TextField("", skin)
        table.add(descriptionTextField)
            .growX()

        table.row()
        table.add(Label("x:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        xPosTextField = TextField("", skin)
        table.add(xPosTextField)
            .growX()

        table.row()
        table.add(Label("y:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        yPosTextField = TextField("", skin)
        table.add(yPosTextField)
            .growX()

        table.row()
        table.add(Label("z:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        zPosTextField = TextField("", skin)
        table.add(zPosTextField)
            .growX()

        return table
    }

    private fun createTagsSection(): Table {
        val table = BorderedTable(skin.getColor("table-border"))
            .padLeft(5f)
            .padRight(5f)

        table.add(Label("Tags", skin))
            .space(5f)

        table.row()
        tagsTable = Table()
        table.add(tagsTable)

        table.row()

        val newTagField = TextField("", skin)
        table.add(newTagField)
            .growX()
            .left()

        val addTagButton = TextButton("Add", skin)
        table.add(addTagButton)
        addTagButton.onChanged {
//            if (!newTagField.text.isNullOrBlank()) {
//                fire(CreateTagEvent(newTagField.text))
//            }
        }

        return table
    }

    private fun createBuildingSection(): Table {
        val table = BorderedTable(skin.getColor("table-border"))
            .padLeft(5f)
            .padRight(5f)
            .left()

        table.add(Label("Buildings", skin))
            .space(5f)
            .grow()

        table.row()
        table.add(Label("Selected:", skin))
            .left()

        buildingSelectBox = SelectBox<String>(skin)
        table.add(buildingSelectBox)
            .grow()
            .padRight(5f)

        val applyButton = TextButton("Apply", skin)
        applyButton.onChanged {
//            if (buildingSelectBox.selected != null) {
//                fire(ApplyBuildingEvent(buildingSelectBox.selected))
//            }
        }
        table.add(applyButton)

        return table
    }
}
