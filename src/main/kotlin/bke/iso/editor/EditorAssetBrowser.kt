package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorAssetBrowser(
    private val skin: Skin,
    private val assets: Assets
) {

    private lateinit var browser: Table
    private lateinit var scrollPane: ScrollPane

    fun create(): Actor {
        setup()

        val root = Table()
        root.background = skin.getDrawable("bg")

        root.add(createTabs())
            .expandX()

        root.row()

        val browserContainer = BorderedTable(skin.getColor("table-border"))
        browserContainer.borderSize = 4f
        root.add(browserContainer)
            .grow()

        browser = Table().top().left()
        scrollPane = ScrollPane(browser)
        scrollPane.layout()
        browserContainer.add(scrollPane)
            .pad(browserContainer.borderSize) // don't let borders cover the content
            .grow()

        populateBrowser()
        return root
    }

    private fun setup() {
        skin.add("asset-browser-tab", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.newTintedDrawable("pixel", "button-up")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
            checked = skin.newTintedDrawable("pixel", "button-checked")
        })
    }

    private fun createTabs(): Table {
        val table = Table()

        val tileButton = TextButton("Tiles", skin, "asset-browser-tab")
        tileButton.padLeft(Value.percentWidth(.25f, tileButton))
        tileButton.padRight(Value.percentWidth(.25f, tileButton))
        table.add(tileButton)

        val actorButton = TextButton("Actors", skin, "asset-browser-tab")
        actorButton.padLeft(Value.percentWidth(.25f, actorButton))
        actorButton.padRight(Value.percentWidth(.25f, actorButton))
        table.add(actorButton)

        ButtonGroup<TextButton>().add(tileButton, actorButton)
        return table
    }

    private fun populateBrowser() {
        val buttons = mutableListOf<ImageTextButton>()
        for (prefab in assets.getAll<ActorPrefab>()) {
            val texture = prefab.components
                .filterIsInstance<Sprite>()
                .firstOrNull()
                ?.let { sprite -> assets.get<Texture>(sprite.texture) }
                ?: continue
            buttons.add(createAssetButton(prefab.name, texture, skin))
        }

        for (row in buttons.chunked(2)) {
            for (button in row) {
                browser.add(button)
                    .uniform()
                    .fill()
                    .pad(10f)
            }
            browser.row()
        }
    }

    private fun createAssetButton(name: String, texture: Texture, skin: Skin): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            imageUp = TextureRegionDrawable(TextureRegion(texture))
            over = skin.newTintedDrawable("pixel", "button-over")
            down = skin.newTintedDrawable("pixel", "button-down")
            checked = skin.newTintedDrawable("pixel", "button-checked")
            font = skin.getFont("default")
        }

        val button = ImageTextButton(name, style)
        // align label to bottom instead of right by default
        val img = button.image
        val label = button.label
        button.clearChildren()
        button.add(img)
        button.row()
        button.add(label)
        return button
    }
}
