package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table

class LoadingScreen(private val assets: Assets) : UIScreen() {

    override fun create() {
        setup()

        val table = Table()
        table.bottom().right()
        table.setFillParent(true)
        table.background = skin.newDrawable("pixel", Color.BLACK)
        stage.addActor(table)

        table.add(Label("Loading...", skin))
            .padRight(50f)
            .padBottom(50f)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("default", assets.fonts[FontOptions("roboto", 45f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
        })
    }
}
