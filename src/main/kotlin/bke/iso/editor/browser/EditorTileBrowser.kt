package bke.iso.editor.browser

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorTileBrowser(
    private val assets: Assets,
    private val skin: Skin
) {

    private val root = Table().top().left()
    private val scrollPane = ScrollPane(root)
    private val buttonGroup = ButtonGroup<ImageTextButton>()

    fun create(): Actor {
        return scrollPane
    }

    fun setVisible(visible: Boolean) {
        scrollPane.isVisible = visible
    }

    fun populate(prefabs: List<TilePrefab>) {
        root.clearChildren()
        buttonGroup.clear()

        val buttons = mutableListOf<ImageTextButton>()
        for (prefab in prefabs) {
            val texture = assets.get<Texture>(prefab.texture)
            buttons.add(createAssetButton(prefab.name, texture, skin))
        }

        for (row in buttons.chunked(2)) {
            for (button in row) {
                root.add(button)
                    .uniform()
                    .fill()
                    .pad(10f)
                buttonGroup.add(button)
            }
            root.row()
        }

        buttonGroup.uncheckAll()
        scrollPane.layout()
    }

    private fun createAssetButton(name: String, texture: Texture, skin: Skin): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            imageUp = TextureRegionDrawable(TextureRegion(texture))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        return ImageTextButton(name, style).apply {
            // align label to bottom instead of right by default
            clearChildren()
            add(image)
            row()
            add(label)
        }
    }
}
