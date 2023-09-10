package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class EditorAssetBrowser(
    private val skin: Skin,
    private val assets: Assets
) {

    fun create(): Actor {
        setup()

        val root = Table()
        root.background = skin.getDrawable("bg")

        root.add(createTabs())
            .expandX()
            .top()
            .left()

        root.row()

        val browserContainer = BorderedTable(skin.getColor("table-border"))
        browserContainer.borderSize = 4f
        root.add(browserContainer)
            .grow()
            .top()
            .left()

//        root.add(createTabs())
//            .expand()
//        root.row()
//        root.add(createAssetBrowser())

//        root.row()
//        val container = BorderedTable(color(77, 100, 130))
//        container.borderSize = 4f
//
//        val browser = Table().top().left()
//        browser.add(createAssetButton("floor", "game/gfx/tiles/floor", skin))
//            .fill()
//
//        val scrollPane = ScrollPane(browser)
//        scrollPane.layout()
//        container.add(scrollPane)
//            .fill()
//            .expand()
//
//        root.add(container)
//            .fill()
//            .expand()

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

    private fun createBrowser(): Table {
        val table = BorderedTable(skin.get<Color>("table-border"))
        table.borderSize = 4f
        return table
    }

    private fun createAssetButton(name: String, texture: String, skin: Skin): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle().apply {
            imageUp = getTextureDrawable(texture)
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

    private fun getTextureDrawable(name: String): TextureRegionDrawable {
        val texture = assets.get<Texture>(name)
        return TextureRegionDrawable(TextureRegion(texture))
    }
}
