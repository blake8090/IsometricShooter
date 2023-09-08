package bke.iso.editor

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
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

        val root = Table()
            .left()
            .top()
        root.debug = false
        root.setFillParent(true)
        stage.addActor(root)

        root.add(menuBar.create())
            .fillX()
            .expandX()

        root.row()
        val editTable = Table()

        editTable.add(createSideBar())
            .width(Value.percentWidth(.15f, root))
            .expandY()
            .fillY()

        editTable.add(toolBar.create())
            .top()
            .expandX()
            .fillX()

        root.add(editTable)
            .left()
            .fill()
            .expand()
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))

        skin.add("default", assets.fonts[FontOptions("ui/roboto", 12f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.getDrawable("bg")
        })

        skin.add("button-up", makePixelTexture(color(20, 51, 82)))
        skin.add("button-over", makePixelTexture(color(34, 84, 133)))
        skin.add("button-down", makePixelTexture(color(43, 103, 161)))
        skin.add("button-checked", makePixelTexture(color(43, 103, 161)))

        skin.add("default", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.getDrawable("button-up")
            down = skin.getDrawable("button-down")
            over = skin.getDrawable("button-over")
        })

        skin.add("sidebarTab", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.getDrawable("button-up")
            down = skin.getDrawable("button-down")
            over = skin.getDrawable("button-over")
            checked = skin.getDrawable("button-checked")
        })
    }

    private fun createSideBar(): Actor {
        val sideBar = BorderedTable(color(77, 100, 130))
        sideBar.left()
        sideBar.borderSize = 2f
        sideBar.background = skin.getDrawable("bg")

        sideBar.add(assetBrowser.create())
            .expand()
            .fill()

        return sideBar
    }
}
