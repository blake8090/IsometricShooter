package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
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

        val pointerButton = ImageButton(getTextureDrawable("ui/editor/pointer"))
        pointerButton.style.over = skin.getDrawable("button-over")
        pointerButton.style.down = skin.getDrawable("button-down")
        pointerButton.style.checked = skin.getDrawable("button-checked")
        pointerButton.pad(12f)
        toolModes.add(pointerButton)

        val brushButton = ImageButton(getTextureDrawable("ui/editor/brush"))
        brushButton.style.over = skin.getDrawable("button-over")
        brushButton.style.down = skin.getDrawable("button-down")
        brushButton.style.checked = skin.getDrawable("button-checked")
        brushButton.pad(12f)
        toolModes.add(brushButton)

        val eraserButton = ImageButton(getTextureDrawable("ui/editor/eraser"))
        eraserButton.style.over = skin.getDrawable("button-over")
        eraserButton.style.down = skin.getDrawable("button-down")
        eraserButton.style.checked = skin.getDrawable("button-checked")
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
        gridButton.style.checked = skin.getDrawable("button-checked")
        gridButton.pad(12f)
        miscTools.add(gridButton)

        val layerLabel = Label("Layer: 1", skin)
        miscTools.add(layerLabel).padLeft(16f)

        val layerDecButton = TextButton("-", skin).apply {
            width = 32f
            padLeft(16f)
            padRight(16f)
        }
        miscTools.add(layerDecButton).padLeft(16f)

        val layerIncButton = TextButton("+", skin).apply {
            width = 32f
            padLeft(16f)
            padRight(16f)
        }
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
