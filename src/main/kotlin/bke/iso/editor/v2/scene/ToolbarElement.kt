package bke.iso.editor.v2.scene

import bke.iso.editor.scene.camera.ToggleHideWallsEvent
import bke.iso.editor.scene.layer.DecreaseLayerEvent
import bke.iso.editor.scene.layer.IncreaseLayerEvent
import bke.iso.editor.scene.layer.ToggleHighlightLayerEvent
import bke.iso.editor.scene.layer.ToggleUpperLayersHiddenEvent
import bke.iso.editor.scene.tool.SelectBrushToolEvent
import bke.iso.editor.scene.tool.SelectEraserToolEvent
import bke.iso.editor.scene.tool.SelectFillToolEvent
import bke.iso.editor.scene.tool.SelectPointerToolEvent
import bke.iso.editor.scene.tool.SelectRoomToolEvent
import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.UIElement
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class ToolbarElement(skin: Skin, private val assets: Assets) : UIElement(skin) {

    private val root = Table()
    private val content = BorderedTable(color(43, 103, 161)).apply {
        top()
        left()
    }

    private lateinit var layerLabel: Label

    override fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Toolbar", skin, "light"))
            .left()
            .expandX()

        val pointerButton = createButton("pointer.png")
        pointerButton.onChanged {
            if (isChecked) {
                fire(SelectPointerToolEvent())
            }
        }
        content.add(pointerButton)
            .padLeft(5f)

        val brushButton = createButton("brush.png")
        brushButton.onChanged {
            if (isChecked) {
                fire(SelectBrushToolEvent())
            }
        }
        content.add(brushButton)

        val eraserButton = createButton("eraser.png")
        eraserButton.onChanged {
            if (isChecked) {
                fire(SelectEraserToolEvent())
            }
        }
        content.add(eraserButton)

        val roomButton = createButton("icon-room.png")
        roomButton.onChanged {
            if (isChecked) {
                fire(SelectRoomToolEvent())
            }
        }
        content.add(roomButton)

        val fillButton = createButton("fill.png")
        fillButton.onChanged {
            if (isChecked) {
                fire(SelectFillToolEvent())
            }
        }
        content.add(fillButton)

        ButtonGroup(pointerButton, brushButton, eraserButton, roomButton, fillButton)

        val gridButton = createButton("grid.png")
        gridButton.style.checked = newTextureDrawable("grid.png", "button-checked")
        content.add(gridButton).spaceLeft(30f)

        layerLabel = Label("", skin)
        content.add(layerLabel).spaceLeft(30f)

        val decreaseLayerButton = createButton("minus.png").apply {
            style.up = newTextureDrawable("minus.png", "button-up")
            style.checked = null

            onChanged {
                fire(DecreaseLayerEvent())
            }
        }
        content.add(decreaseLayerButton).space(10f)

        val increaseLayerButton = createButton("plus.png").apply {
            style.up = newTextureDrawable("plus.png", "button-up")
            style.checked = null

            onChanged {
                fire(IncreaseLayerEvent())
            }
        }
        content.add(increaseLayerButton).space(10f)

        val hideUpperLayersButton = createButton("hide-layers.png")
        hideUpperLayersButton.onChanged {
            fire(ToggleUpperLayersHiddenEvent())
        }
        content.add(hideUpperLayersButton).space(10f)

        val hideWallsButton = createButton("hide-walls.png")
        hideWallsButton.onChanged {
            fire(ToggleHideWallsEvent())
        }
        content.add(hideWallsButton).space(10f)

        val highlightLayerButton = createButton("highlight-layer.png")
        highlightLayerButton.onChanged {
            fire(ToggleHighlightLayerEvent())
        }
        content.add(highlightLayerButton)

        root.row()
        root.add(content).grow()

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
}
