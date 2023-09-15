package bke.iso.editor.ui.browser

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.newTintedDrawable
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

    private val buttonGroup = ButtonGroup<ActorPrefabButton>()
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

        val buttons = mutableListOf<ActorPrefabButton>()
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

    private fun createButton(prefab: ActorPrefab, sprite: Sprite): ActorPrefabButton {
        val texture = assets.get<Texture>(sprite.texture)

        val style = ImageTextButton.ImageTextButtonStyle().apply {
            imageUp = TextureRegionDrawable(TextureRegion(texture))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        return ActorPrefabButton(prefab, sprite.texture, style)
    }
}

private class ActorPrefabButton(
    val prefab: ActorPrefab,
    val texture: String,
    style: ImageTextButtonStyle
) : ImageTextButton(prefab.name, style) {

    init {
        // align label to bottom instead of right by default
        clearChildren()
        add(image)
        row()
        add(label)
    }
}
