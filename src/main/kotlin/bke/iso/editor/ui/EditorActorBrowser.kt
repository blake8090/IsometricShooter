package bke.iso.editor.ui

import bke.iso.editor.SelectActorPrefabEvent
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.newTintedDrawable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

private const val BUTTONS_PER_ROW = 2

class EditorActorBrowser(
    private val assets: Assets,
    private val skin: Skin
) {

    private val buttonGroup = ButtonGroup<ImageTextButton>()
    private val content = Table().top().left()
    val root = ScrollPane(content)

    var visible: Boolean
        get() = root.isVisible
        set(value) {
            root.isVisible = value
        }

    fun populate(prefabs: List<ActorPrefab>) {
        content.clearChildren()
        buttonGroup.clear()

        val buttons = mutableListOf<ImageTextButton>()
        for (prefab in prefabs) {
            val sprite = prefab
                .components
                .firstNotNullOfOrNull { component -> component as? Sprite }
                ?: continue
            buttons.add(createButton(prefab, sprite))
        }

        for (row in buttons.chunked(BUTTONS_PER_ROW)) {
            for (button in row) {
                content.add(button)
                    .uniform()
                    .fill()
                    .pad(10f)
                buttonGroup.add(button)
            }
            content.row()
        }

        buttonGroup.uncheckAll()
        root.layout()
    }

    private fun createButton(prefab: ActorPrefab, sprite: Sprite): ImageTextButton {
        val texture = assets.get<Texture>(sprite.texture)

        val style = ImageTextButton.ImageTextButtonStyle().apply {
            imageUp = TextureRegionDrawable(TextureRegion(texture))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        return ImageTextButton(prefab.name, style).apply {
            // align label to bottom instead of right by default
            clearChildren()
            add(image)
            row()
            add(label)

            onChanged {
                if (isChecked) {
                    fire(SelectActorPrefabEvent(prefab, sprite))
                }
            }
        }
    }

    fun unselect() {
        buttonGroup.uncheckAll()
    }
}