package bke.iso.editor.scene.ui

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox.SelectBoxStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField

class SceneInspectorView(
    private val skin: Skin,
    private val assets: Assets
) {

    private val root = Table()
        .top()
        .padLeft(5f)
        .padRight(5f)

    private lateinit var idTextField: TextField
    private lateinit var descriptionTextField: TextField
    private lateinit var xPosTextField: TextField
    private lateinit var yPosTextField: TextField
    private lateinit var zPosTextField: TextField

    fun create(): Actor {
        setup()

        root.background = skin.getDrawable("bg")

        root.add(Label("Inspector", skin))
            .left()

        root.row()
        root.add(createPropertiesSection())
            .growX()
            .padTop(15f)

        root.row()
        root.add(createTagsSection())
            .growX()
            .padTop(15f)

        root.row()
        root.add(createBuildingSection())
            .growX()
            .padTop(15f)
        return root
    }

    fun updateProperties(
        id: String,
        description: String,
        pos: Vector3
    ) {
        idTextField.text = id
        descriptionTextField.text = description
        xPosTextField.text = pos.x.toString()
        yPosTextField.text = pos.y.toString()
        zPosTextField.text = pos.z.toString()
    }

    private fun setup() {
        skin.add("sceneInspector", TextField.TextFieldStyle().apply {
            font = skin.getFont("default")
            fontColor = Color.WHITE
            focusedFontColor = Color.WHITE

            background = skin.newDrawable("pixel", Color.BLACK)
            focusedBackground = skin.newDrawable("pixel", Color.BLACK)

            cursor = skin.newDrawable("pixel", color(50, 158, 168))
            selection = skin.newDrawable("pixel", color(50, 158, 168))
        })

        skin.add("sceneInspector", TextButton.TextButtonStyle().apply {
//            font = assets.fonts[FontOptions("roboto.ttf", 14f, Color.WHITE)]
            font = skin.getFont("default")
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
//            checked = skin.newDrawable("pixel", color(43, 103, 161))
        })
    }

    private fun createPropertiesSection(): Table {
        val table = BorderedTable(color(43, 103, 161))
            .padLeft(5f)
            .padRight(5f)

        table.add(Label("id:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        idTextField = TextField("", skin, "sceneInspector")
        idTextField.isDisabled = true
        table.add(idTextField)
            .growX()

        table.row()
        table.add(Label("description:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        descriptionTextField = TextField("", skin, "sceneInspector")
        table.add(descriptionTextField)
            .growX()

        table.row()
        table.add(Label("x:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        xPosTextField = TextField("", skin, "sceneInspector")
        table.add(xPosTextField)
            .growX()

        table.row()
        table.add(Label("y:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        yPosTextField = TextField("", skin, "sceneInspector")
        table.add(yPosTextField)
            .growX()

        table.row()
        table.add(Label("z:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        zPosTextField = TextField("", skin, "sceneInspector")
        table.add(zPosTextField)
            .growX()

        return table
    }

    private fun createTagsSection(): Table {
        val table = BorderedTable(color(43, 103, 161))
            .padLeft(5f)
            .padRight(5f)

        table.add(Label("Tags", skin))
            .colspan(2)
            .left()

        table.row()
        table.add(Label("example:tag", skin))
            .left()
            .padRight(5f)
        table.add(TextButton("Delete", skin, "sceneInspector"))

        table.row()
        table.add(Label("scene:mission-01.scene", skin))
            .left()
            .padRight(5f)
        table.add(TextButton("Delete", skin, "sceneInspector"))

        table.row()
        table.add(Label("health:100", skin))
            .left()
            .padRight(5f)
        table.add(TextButton("Delete", skin, "sceneInspector"))

        table.row()
        table.add(TextField("My new tag", skin, "sceneInspector"))
            .left()
            .growX()
        table.add(TextButton("Add", skin, "sceneInspector"))

        return table
    }

    private fun createBuildingSection(): Table {
        val table = BorderedTable(color(43, 103, 161))
            .padLeft(5f)
            .padRight(5f)

        table.add(Label("Selected Building:", skin))
            .left()

        val selectBox = SelectBox<String>(SelectBoxStyle().apply {
            font = skin.getFont("default")
            listStyle = List.ListStyle().apply {
                selection = skin.newTintedDrawable("pixel", "button-over")
                scrollStyle = ScrollPane.ScrollPaneStyle().apply {
                    font = skin.getFont("default")
                    background = skin.getDrawable("bg")
                }
            }
        })
        selectBox.setItems("None", "Building #1", "Building #2", "Building #3")
        table.add(selectBox)
            .padLeft(5f)

        return table
    }
}
