package bke.iso.editor.browser

import com.badlogic.gdx.scenes.scene2d.ui.Label
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.loader.ActorPrefab
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
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

    fun create(): Actor {
        root.add(Label("TILES WILL GO HERE", skin))
        scrollPane.layout()
        return scrollPane
    }

    fun setVisible(visible: Boolean) {
        scrollPane.isVisible = visible
    }

//    fun populate(prefabs: List<ActorPrefab>) {
//        root.clearChildren()
//
//        val buttons = mutableListOf<ImageTextButton>()
//        for (prefab in prefabs) {
//            val texture = getPrefabTexture(prefab) ?: continue
//            buttons.add(createAssetButton(prefab.name, texture, skin))
//        }
//
//        for (row in buttons.chunked(2)) {
//            for (button in row) {
//                root.add(button)
//                    .uniform()
//                    .fill()
//                    .pad(10f)
//            }
//            root.row()
//        }
//    }
//
//    private fun getPrefabTexture(prefab: ActorPrefab) =
//        prefab.components
//            .filterIsInstance<Sprite>()
//            .firstOrNull()
//            ?.let { sprite -> assets.get<Texture>(sprite.texture) }
//
//    private fun createAssetButton(name: String, texture: Texture, skin: Skin): ImageTextButton {
//        val style = ImageTextButton.ImageTextButtonStyle().apply {
//            imageUp = TextureRegionDrawable(TextureRegion(texture))
//            over = skin.newTintedDrawable("pixel", "button-over")
//            down = skin.newTintedDrawable("pixel", "button-down")
//            checked = skin.newTintedDrawable("pixel", "button-checked")
//            font = skin.getFont("default")
//        }
//
//        return ImageTextButton(name, style).apply {
//            // align label to bottom instead of right by default
//            val buttonImg = image
//            val buttonLabel = label
//            clearChildren()
//
//            add(buttonImg)
//            row()
//            add(label)
//        }
//    }
}
