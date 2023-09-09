package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorAssetBrowser(
    private val skin: Skin,
    private val assets: Assets
) {

    fun create(): Actor {
        setup()

        val root = Table().top().left()

        val tabs = HorizontalGroup()
        val tileButton = TextButton("Tiles", skin, "asset-browser-tab").apply {
            padLeft(16f)
            padRight(16f)
        }
        tabs.addActor(tileButton)

        val actorButton = TextButton("Actors", skin, "asset-browser-tab").apply {
            padLeft(16f)
            padRight(16f)
        }
        tabs.addActor(actorButton)

        ButtonGroup<TextButton>().add(tileButton, actorButton)
        root.add(tabs).left()

        root.row()
        val container = BorderedTable(color(77, 100, 130))
        container.borderSize = 4f

        val browser = Table().top().left()
        browser.add(createAssetButton("floor", "game/gfx/tiles/floor", skin))
            .fill()

        val scrollPane = ScrollPane(browser)
        scrollPane.layout()
        container.add(scrollPane)
            .fill()
            .expand()

        root.add(container)
            .fill()
            .expand()

        return root
    }

    private fun setup() {
        skin.add("asset-browser-tab", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.getDrawable("button-up")
            down = skin.getDrawable("button-down")
            over = skin.getDrawable("button-over")
            checked = skin.getDrawable("button-checked")
        })
    }

    private fun createAssetButton(name: String, texture: String, skin: Skin): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            imageUp = getTextureDrawable(texture)
            over = skin.getDrawable("button-over")
            down = skin.getDrawable("button-down")
            checked = skin.getDrawable("button-checked")
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

    private fun getTextureDrawable(name: String): TextureRegionDrawable {
        val texture = assets.get<Texture>(name)
        return TextureRegionDrawable(TextureRegion(texture))
    }
}
