package bke.iso.editor.main

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorToolBar(
    private val skin: Skin,
    private val assets: Assets
) {

    fun create(): Table {
        val root = BorderedTable(skin.getColor("table-border"))
        root.left()
        root.padTop(5f)
        root.padBottom(5f)
        root.borderSize = 1f
        root.borderLeft = false
        root.background = skin.getDrawable("bg")

        val pointerButton = createButton("pointer.png")
        root.add(pointerButton)
            .padLeft(5f)

        val brushButton = createButton("brush.png")
        root.add(brushButton)

        val eraserButton = createButton("eraser.png")
        root.add(eraserButton)

        ButtonGroup(pointerButton, brushButton, eraserButton)

        val gridButton = createButton("grid.png")
        gridButton.style.checked = newTextureDrawable("grid.png", "button-checked")
        root.add(gridButton).spaceLeft(30f)

        root.add(Label("Layer: 1", skin)).spaceLeft(30f)

        val decreaseLayerButton = createButton("minus.png").apply {
            style.up = newTextureDrawable("minus.png", "button-up")
        }
        root.add(decreaseLayerButton).space(20f)

        val increaseLayerButton = createButton("plus.png").apply {
            style.up = newTextureDrawable("plus.png", "button-up")
        }
        root.add(increaseLayerButton)

        return root
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
        canvas.setColor(skin.getColor(bgColor))
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