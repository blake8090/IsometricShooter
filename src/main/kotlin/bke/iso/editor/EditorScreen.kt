package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.util.TextButtonBuilder
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

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

        editTable.add(createSideBar())
            .width(Value.percentWidth(.15f, root))
            .expandY()
            .fillY()

        editTable.add(createToolBar())
            .top()
            .expandX()
            .fillX()

        root.add(editTable)
            .left()
            .fill()
            .expand()
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))

        skin.add("font", assets.fonts[FontOptions("ui/roboto", 25f, Color.WHITE)])

        skin.add("toolbar-checked", makePixelTexture(color(43, 103, 161)))

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("font")
            background = skin.getDrawable("bg")
        })

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
        val sideBar = BorderedTable(color(77, 100, 130))
        sideBar.borderSize = 2f
        sideBar.background = skin.getDrawable("bg")

        return sideBar
    }

    private fun createToolBar(): Actor {
        val toolBar =  BorderedTable(color(77, 100, 130))
        toolBar.left()
        toolBar.borderSize = 2f
        toolBar.borderLeft = false
        toolBar.borderRight = false
        toolBar.background = skin.getDrawable("bg")

        val toolModes = Table()

        val pointerButton = ImageButton(getTextureDrawable("ui/editor/pointer"))
        pointerButton.style.checked = skin.getDrawable("toolbar-checked")
        pointerButton.pad(12f)
        toolModes.add(pointerButton)

        val brushButton = ImageButton(getTextureDrawable("ui/editor/brush"))
        brushButton.style.checked = skin.getDrawable("toolbar-checked")
        brushButton.pad(12f)
        toolModes.add(brushButton)

        val eraserButton = ImageButton(getTextureDrawable("ui/editor/eraser"))
        eraserButton.style.checked = skin.getDrawable("toolbar-checked")
        eraserButton.pad(12f)
        toolModes.add(eraserButton)

        ButtonGroup<ImageButton>()
            .add(pointerButton, brushButton, eraserButton)

        toolBar.add(toolModes)

        val miscTools = BorderedTable(color(77, 100, 130))
        miscTools.left()
        miscTools.borderSize = 2f
        miscTools.borderRight = false
        miscTools.borderTop = false
        miscTools.borderBottom = false

        val gridButton = ImageButton(getTextureDrawable("ui/editor/grid"))
        gridButton.style.checked = skin.getDrawable("toolbar-checked")
        gridButton.pad(12f)
        miscTools.add(gridButton)

        val layerLabel = Label("Layer: 1", skin)
        miscTools.add(layerLabel).padLeft(16f)

        val layerDecButton = TextButtonBuilder("-", skin).build()
        layerDecButton.width = 32f
        layerDecButton.padLeft(16f)
        layerDecButton.padRight(16f)
        miscTools.add(layerDecButton).padLeft(16f)

        val layerIncButton = TextButtonBuilder("+", skin).build()
        layerIncButton.width = 32f
        layerIncButton.padLeft(16f)
        layerIncButton.padRight(16f)
        miscTools.add(layerIncButton).padLeft(8f)

        toolBar.add(miscTools)
            .fill()
            .expand()

        return toolBar
    }

    private fun getTextureDrawable(name: String): TextureRegionDrawable {
        val texture = assets.get<Texture>(name)
        return TextureRegionDrawable(TextureRegion(texture))
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

private class BorderedTable(borderColor: Color) : Table() {

    private var pixel = makePixelTexture(borderColor)

    var borderSize: Float = 1f
    var borderLeft: Boolean = true
    var borderRight: Boolean = true
    var borderTop: Boolean = true
    var borderBottom: Boolean = true

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        // TODO: similar to figma, add inside, outside and center options for borders

        if (borderTop) {
            batch.draw(pixel, x, y + height - borderSize, width, borderSize)
        }

        if (borderBottom) {
            batch.draw(pixel, x, y, width, borderSize)
        }

        if (borderLeft) {
            batch.draw(pixel, x, y, borderSize, height)
        }

        if (borderRight) {
            batch.draw(pixel, x + width - borderSize, y, borderSize, height)
        }
    }
}
