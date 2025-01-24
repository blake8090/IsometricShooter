package bke.iso.editor.v3.scene

import bke.iso.editor.scene.tool.SelectActorPrefabEvent
import bke.iso.editor.scene.tool.SelectTilePrefabEvent
import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.ui.v2.UIView
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align

class AssetBrowserView(
    private val skin: Skin,
    private val assets: Assets
) : UIView() {

    private val content = BorderedTable(color(43, 103, 161)).apply {
        top()
        left()
    }

    override fun create() {
        root.background = skin.getDrawable("bg")

        root.add(Label("Asset Browser", skin, "light"))
            .left()

        root.row()
        root.add(content).grow()
    }

    fun update(assetList: List<Any>) {
        content.clearChildren()
        for (asset in assetList) {
            if (asset is TilePrefab) {
                createTilePrefabButton(asset)
            } else if (asset is ActorPrefab) {
                createActorPrefabButton(asset)
            }
        }
    }

    private fun createTilePrefabButton(asset: TilePrefab) {
        content.row()
        content
            .add(createButton(asset, skin))
            .left()
            .growX()
    }

    private fun createActorPrefabButton(asset: ActorPrefab) {
        val sprite = asset
            .components
            .firstNotNullOfOrNull { component -> component as? Sprite }
            ?: return
        content.row()
        content
            .add(createButton(asset, skin, sprite))
            .left()
            .growX()
    }

    private fun createButton(prefab: TilePrefab, skin: Skin): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            val texture = assets.get<Texture>(prefab.sprite.texture)
            imageUp = TextureRegionDrawable(TextureRegion(texture))

            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        return ImageTextButton(prefab.name, style).apply {
            align(Align.left)
            onChanged {
                if (isChecked) {
                    fire(SelectTilePrefabEvent(prefab))
                }
            }
        }
    }

    private fun createButton(prefab: ActorPrefab, skin: Skin, sprite: Sprite): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            val texture = assets.get<Texture>(sprite.texture)
            imageUp = TextureRegionDrawable(TextureRegion(texture))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        return ImageTextButton(prefab.name, style).apply {
            align(Align.left)
            onChanged {
                if (isChecked) {
                    fire(SelectActorPrefabEvent(prefab))
                }
            }
        }
    }
}
