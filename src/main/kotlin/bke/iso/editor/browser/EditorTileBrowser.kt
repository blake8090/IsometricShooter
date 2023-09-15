package bke.iso.editor.browser

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
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

    private val buttonGroup = ButtonGroup<TilePrefabButton>()
    private val content = Table().top().left()
    val root = ScrollPane(content)

    var visible: Boolean
        get() = root.isVisible
        set(value) {
            root.isVisible = value
        }

    fun getSelectedPrefab(): TilePrefabData? =
        buttonGroup.checked?.selectedPrefab

    fun populate(prefabs: List<TilePrefab>) {
        content.clearChildren()
        buttonGroup.clear()

        for (row in prefabs.chunked(2)) {
            for (prefab in row) {
                populate(prefab)
            }
            content.row()
        }

        buttonGroup.uncheckAll()
        root.layout()
    }

    private fun populate(prefab: TilePrefab) {
        val button = createButton(prefab, skin)
        buttonGroup.add(button)
        content.add(button)
            .uniform()
            .fill()
            .pad(10f)
    }

    private fun createButton(prefab: TilePrefab, skin: Skin): TilePrefabButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            val texture = assets.get<Texture>(prefab.texture)
            imageUp = TextureRegionDrawable(TextureRegion(texture))

            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        return TilePrefabButton(TilePrefabData(prefab, prefab.texture), style)
    }
}

private class TilePrefabButton(
    val selectedPrefab: TilePrefabData,
    style: ImageTextButtonStyle
) : ImageTextButton(selectedPrefab.prefab.name, style) {

    init {
        // align label to bottom instead of right by default
        clearChildren()
        add(image)
        row()
        add(label)
    }
}
