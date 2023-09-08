package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.util.TextButtonBuilder
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorScreen(assets: Assets) : UIScreen(assets) {

    private val assetBrowser = EditorAssetBrowser(assets)

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

        skin.add("font", assets.fonts[FontOptions("ui/roboto", 20f, Color.WHITE)])

        skin.add("toolbar-over", makePixelTexture(color(34, 84, 133)))
        skin.add("toolbar-down", makePixelTexture(color(43, 103, 161)))
        skin.add("toolbar-checked", makePixelTexture(color(43, 103, 161)))

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("font")
            background = skin.getDrawable("bg")
        })

        skin.add("default", TextButton.TextButtonStyle().apply {
            font = skin.getFont("font")
            up = skin.newDrawable("pixel", color(20, 51, 82))
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
        })

        skin.add("menu", TextButton.TextButtonStyle().apply {
            font = skin.getFont("font")
            up = skin.newDrawable("pixel", Color.DARK_GRAY)
            down = skin.newDrawable("pixel", Color.GRAY)
            over = skin.newDrawable("pixel", color(94, 94, 94))
        })

        skin.add("sidebarTab", TextButton.TextButtonStyle().apply {
            font = skin.getFont("font")
            up = skin.newDrawable("pixel", color(20, 51, 82))
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
            checked = skin.newDrawable("pixel", color(43, 103, 161))
        })
    }

    private fun createMenuBar(): Actor {
        val menuBar = Table().left()
        menuBar.background = skin.newDrawable("pixel", Color.DARK_GRAY)

        val newButton = textButton("New", skin, "menu")
        newButton.pad(6f)
        menuBar.add(newButton)

        val openButton = textButton("Open", skin, "menu")
        openButton.pad(6f)
        menuBar.add(openButton)

        val saveButton = textButton("Save", skin, "menu")
        saveButton.pad(6f)
        menuBar.add(saveButton)

        return menuBar
    }

    private fun createSideBar(): Actor {
        val sideBar = BorderedTable(color(77, 100, 130))
        sideBar.left()
        sideBar.borderSize = 2f
        sideBar.background = skin.getDrawable("bg")

        sideBar.add(assetBrowser.create(skin))
            .expand()
            .fill()

        return sideBar
    }

    private fun createToolBar(): Actor {
        val toolBar = BorderedTable(color(77, 100, 130))
        toolBar.left()
        toolBar.borderSize = 2f
        toolBar.borderLeft = false
        toolBar.borderRight = false
        toolBar.background = skin.getDrawable("bg")

        val toolModes = Table()

        val pointerButton = ImageButton(getTextureDrawable("ui/editor/pointer"))
        pointerButton.style.over = skin.getDrawable("toolbar-over")
        pointerButton.style.down = skin.getDrawable("toolbar-down")
        pointerButton.style.checked = skin.getDrawable("toolbar-checked")
        pointerButton.pad(12f)
        toolModes.add(pointerButton)

        val brushButton = ImageButton(getTextureDrawable("ui/editor/brush"))
        brushButton.style.over = skin.getDrawable("toolbar-over")
        brushButton.style.down = skin.getDrawable("toolbar-down")
        brushButton.style.checked = skin.getDrawable("toolbar-checked")
        brushButton.pad(12f)
        toolModes.add(brushButton)

        val eraserButton = ImageButton(getTextureDrawable("ui/editor/eraser"))
        eraserButton.style.over = skin.getDrawable("toolbar-over")
        eraserButton.style.down = skin.getDrawable("toolbar-down")
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
}