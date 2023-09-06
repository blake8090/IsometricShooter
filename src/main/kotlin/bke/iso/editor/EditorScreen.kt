package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.util.TextButtonBuilder
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class EditorScreen(private val assets: Assets) : UIScreen() {

    override fun create() {
        setup()

        val root = Table()
            .left()
            .top()
        root.debug = false
        root.setFillParent(true)
        stage.addActor(root)

        root.add(createMenuBar())
            .fillX()
            .expandX()

        root.row()
        val editTable = Table()
//        editTable.debug = true

        editTable.add(createSideBar())
            .width(Value.percentWidth(.15f, root))
            .expandY()
            .fillY()

        editTable.add(createToolBar())
            .top()
            .expandX()
            .fillX()
            .height(75f)

        root.add(editTable)
            .left()
            .fill()
            .expand()
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("font", assets.fonts[FontOptions("ui/roboto", 25f, Color.WHITE)])

//        skin.add("default", Label.LabelStyle().apply {
//            font = skin.getFont("title")
//            background = skin.newDrawable("white", Color.DARK_GRAY)
//        })
//
        skin.add("default", TextButton.TextButtonStyle().apply {
            font = skin.getFont("font")
            up = skin.newDrawable("pixel", Color.DARK_GRAY)
            down = skin.newDrawable("pixel", Color.GRAY)
            over = skin.newDrawable("pixel", color(94, 94, 94))
        })
    }

    private fun createMenuBar(): Actor {
        val menuBar = Table().left()
        menuBar.background = skin.newDrawable("pixel", Color.DARK_GRAY)

        val newButton = TextButtonBuilder("New", skin).build()
        newButton.pad(6f)
        menuBar.add(newButton)

        val openButton = TextButtonBuilder("Open", skin).build()
        openButton.pad(6f)
        menuBar.add(openButton)

        val saveButton = TextButtonBuilder("Save", skin).build()
        saveButton.pad(6f)
        menuBar.add(saveButton)

        return menuBar
    }

    private fun createSideBar(): Actor {
        val sideBar = BorderedTable(2f, color(77, 100, 130)).left()
        sideBar.background = skin.newDrawable("pixel", Color.DARK_GRAY)

        return sideBar
    }

    private fun createToolBar(): Actor {
        val toolBar = BorderedTable(2f, color(77, 100, 130)).left()
        toolBar.background = skin.newDrawable("pixel", Color.DARK_GRAY)

        return toolBar
    }

    /**
     * Utility to create a [Color] from RGBA values provided in a 0 to 255 range
     */
    private fun color(r: Int, g: Int, b: Int, a: Int = 255): Color =
        Color(
            (r / 255f).coerceIn(0f, 1f),
            (g / 255f).coerceIn(0f, 1f),
            (b / 255f).coerceIn(0f, 1f),
            (a / 255f).coerceIn(0f, 1f)
        )
}

private class BorderedTable(private val borderSize: Float, borderColor: Color) : Table() {

    private val pixel = makePixelTexture(borderColor)

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        // TODO: similar to figma, add inside, outside and center options for borders
        //  also add options for which sides of the borders are drawn
        batch.draw(pixel, x, y + height - borderSize, width, borderSize) // top
        batch.draw(pixel, x, y, width, borderSize) // bottom
        batch.draw(pixel, x, y, borderSize, height) // left
        batch.draw(pixel, x + width - borderSize, y, borderSize, height) // right
    }
}
