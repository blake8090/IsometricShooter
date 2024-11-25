package bke.iso.editorv2.scene.ui

import bke.iso.editorv2.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.graphics.Color
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

    fun create(): Actor {
        setup()

        root.background = skin.getDrawable("bg")

        root.add(Label("Inspector", skin))
            .left()

        root.row()
        root.add(createVariablesSection())
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

    private fun createVariablesSection(): Table {
        val table = BorderedTable(color(43, 103, 161))
            .padLeft(5f)
            .padRight(5f)

        table.add(Label("name:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        table.add(TextField("asd asd", skin, "sceneInspector"))
            .growX()

        table.row()
        table.add(Label("description:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        table.add(TextField("Test description", skin, "sceneInspector"))
            .growX()

        table.row()
        table.add(Label("x:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        table.add(TextField("000.00", skin, "sceneInspector"))
            .growX()

        table.row()
        table.add(Label("y:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        table.add(TextField("000.00", skin, "sceneInspector"))
            .growX()

        table.row()
        table.add(Label("z:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)
        table.add(TextField("000.00", skin, "sceneInspector"))
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
