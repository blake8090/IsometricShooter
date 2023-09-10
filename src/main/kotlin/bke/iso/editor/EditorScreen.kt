package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class EditorScreen(assets: Assets) : UIScreen(assets) {

    private val menuBar = EditorMenuBar(skin)
    private val toolBar = EditorToolBar(skin, assets)
    private val assetBrowser = EditorAssetBrowser(skin, assets)

    override fun create() {
        setup()

        val root = Table().apply {
            setFillParent(true)

            add(menuBar.create())
                .growX()
                .colspan(2)
            row()
            add(assetBrowser.create())
                .width(Value.percentWidth(.15f, this))
                .growY()
                .top()
                .left()
            add(toolBar.create())
                .expand()
                .fillX()
                .top()
                .left()
        }

        stage.addActor(root)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))

        skin.add("default", assets.fonts[FontOptions("ui/roboto", 13f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.getDrawable("bg")
        })

        skin.add("button-up", color(20, 51, 82))
        skin.add("button-over",color(34, 84, 133))
        skin.add("button-down", color(43, 103, 161))
        skin.add("button-checked", color(43, 103, 161))
        skin.add("table-border", color(77, 100, 130))

        skin.add("default", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.newTintedDrawable("pixel", "button-up")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
        })
    }
}
