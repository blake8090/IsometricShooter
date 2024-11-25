package bke.iso.editorv2.scene.ui

import bke.iso.editor.camera.ToggleHideWallsEvent
import bke.iso.editor.tool.SelectBrushToolEvent
import bke.iso.editor.tool.SelectEraserToolEvent
import bke.iso.editor.tool.SelectFillToolEvent
import bke.iso.editor.tool.SelectPointerToolEvent
import bke.iso.editor.tool.SelectRoomToolEvent
import bke.iso.editorv2.scene.layer.DecreaseLayerEvent
import bke.iso.editorv2.scene.layer.IncreaseLayerEvent
import bke.iso.editorv2.scene.layer.ToggleHighlightLayerEvent
import bke.iso.editorv2.scene.layer.ToggleUpperLayersHiddenEvent
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class SceneToolbarView(
    private val skin: Skin,
    private val assets: Assets
) {

    private lateinit var layerLabel: Label

    fun create(): Table {
        val root = BorderedTable(skin.getColor("table-border"))
        root.left()
        root.padTop(5f)
        root.padBottom(5f)
        root.borderSize = 1f
        root.borderLeft = false
        root.background = skin.getDrawable("bg")

        val pointerButton = createButton("pointer.png")
        pointerButton.onChanged {
            if (isChecked) {
                fire(SelectPointerToolEvent())
            }
        }
        root.add(pointerButton)
            .padLeft(5f)

        val brushButton = createButton("brush.png")
        brushButton.onChanged {
            if (isChecked) {
                fire(SelectBrushToolEvent())
            }
        }
        root.add(brushButton)

        val eraserButton = createButton("eraser.png")
        eraserButton.onChanged {
            if (isChecked) {
                fire(SelectEraserToolEvent())
            }
        }
        root.add(eraserButton)

        val roomButton = createButton("icon-room.png")
        roomButton.onChanged {
            if (isChecked) {
                fire(SelectRoomToolEvent())
            }
        }
        root.add(roomButton)

        val fillButton = createButton("fill.png")
        fillButton.onChanged {
            if (isChecked) {
                fire(SelectFillToolEvent())
            }
        }
        root.add(fillButton)

        ButtonGroup(pointerButton, brushButton, eraserButton, roomButton, fillButton)

        val gridButton = createButton("grid.png")
        gridButton.style.checked = newTextureDrawable("grid.png", "button-checked")
        root.add(gridButton).spaceLeft(30f)

        layerLabel = Label("", skin)
        root.add(layerLabel).spaceLeft(30f)

        val decreaseLayerButton = createButton("minus.png").apply {
            style.up = newTextureDrawable("minus.png", "button-up")
            style.checked = null

            onChanged {
                fire(DecreaseLayerEvent())
            }
        }
        root.add(decreaseLayerButton).space(10f)

        val increaseLayerButton = createButton("plus.png").apply {
            style.up = newTextureDrawable("plus.png", "button-up")
            style.checked = null

            onChanged {
                fire(IncreaseLayerEvent())
            }
        }
        root.add(increaseLayerButton).space(10f)

        val hideUpperLayersButton = createButton("hide-layers.png")
        hideUpperLayersButton.onChanged {
            fire(ToggleUpperLayersHiddenEvent())
        }
        root.add(hideUpperLayersButton).space(10f)

        val hideWallsButton = createButton("hide-walls.png")
        hideWallsButton.onChanged {
            fire(ToggleHideWallsEvent())
        }
        root.add(hideWallsButton).space(10f)

        val highlightLayerButton = createButton("highlight-layer.png")
        highlightLayerButton.onChanged {
            fire(ToggleHighlightLayerEvent())
        }
        root.add(highlightLayerButton)

        return root
    }

    private fun createButton(texture: String): Button {
        val style = Button.ButtonStyle().apply {
            up = newTextureDrawable(texture)
            over = newTextureDrawable(texture, "button-over")
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

    fun updateLayerLabel(layer: Float) {
        layerLabel.setText("Layer: $layer")
    }
}
