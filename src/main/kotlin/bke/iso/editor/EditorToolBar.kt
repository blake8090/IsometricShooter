package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorToolBar(
    private val skin: Skin,
    private val assets: Assets
) {

    fun create(): Table {
        val toolBar = BorderedTable(color(77, 100, 130))
        toolBar.left()
        toolBar.borderSize = 2f
        toolBar.borderLeft = false
        toolBar.borderRight = false
        toolBar.background = skin.getDrawable("bg")

        val toolModes = Table()

        val pointerButton = createButton("ui/editor/pointer")
        toolModes.add(pointerButton)

        val brushButton = createButton("ui/editor/brush")
        toolModes.add(brushButton)

        val eraserButton = createButton("ui/editor/eraser")
        toolModes.add(eraserButton)

        ButtonGroup<Button>().add(pointerButton, brushButton, eraserButton)

        toolBar.add(toolModes)

        val miscTools = BorderedTable(color(77, 100, 130))
        miscTools.left()
        miscTools.borderSize = 2f
        miscTools.borderRight = false
        miscTools.borderTop = false
        miscTools.borderBottom = false

        val gridButton = createButton("ui/editor/grid")
        gridButton.style.checked = newTextureDrawable("ui/editor/grid", "button-checked")
        miscTools.add(gridButton)

        val layerLabel = Label("Layer: 1", skin)
        miscTools.add(layerLabel).padLeft(16f)

        val decreaseLayerButton = createButton("ui/editor/minus").apply {
            style.up = newTextureDrawable("ui/editor/minus", "button-up")
        }
        miscTools.add(decreaseLayerButton)
            .height(Value.percentHeight(.5f, layerLabel))

        val increaseLayerButton = createButton("ui/editor/plus").apply {
            style.up = newTextureDrawable("ui/editor/plus", "button-up")
        }
        miscTools.add(increaseLayerButton)

        toolBar.add(miscTools)
            .fill()
            .expand()

        return toolBar
    }

    private fun createButton(texture: String): Button {
        val style = Button.ButtonStyle().apply {
            up = newTextureDrawable(texture)
            over = newTextureDrawable(texture, "button-over")
            down = newTextureDrawable(texture, "button-down")
            checked = newTextureDrawable(texture, "button-checked")
        }
        return Button(style)
    }

    private fun newTextureDrawable(textureName: String, bgColor: String): TextureRegionDrawable {
        val texture = assets.get<Texture>(textureName)

        val canvas = Pixmap(texture.width, texture.height, Pixmap.Format.RGBA8888)
        val color = skin.get<Color>(bgColor)
        canvas.setColor(color)
        canvas.fill()

        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()
        canvas.setColor(0)
        canvas.drawPixmap(pixmap, 0, 0)

        val canvasTexture = Texture(canvas)
        canvas.dispose()
        pixmap.dispose()

        return TextureRegionDrawable(TextureRegion(canvasTexture))
    }

    private fun newTextureDrawable(textureName: String): TextureRegionDrawable {
        val texture = assets.get<Texture>(textureName)
        return TextureRegionDrawable(TextureRegion(texture))
    }
}
