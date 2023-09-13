package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table

class LoadingScreen(assets: Assets) : UIScreen(assets) {

    override fun create() {
        setup()

        val table = Table()
        table.bottom().right()
        table.setFillParent(true)
        table.background = skin.newDrawable("pixel", Color.BLACK)
        stage.addActor(table)

        val label = Label("Loading...", skin)
        table.add(label)
            .padRight(50f)
            .padBottom(50f)

        label.addAction(
            Actions.repeat(
                RepeatAction.FOREVER,
                Actions.sequence(
                    Actions.moveBy(0f, 50f, 0.5f),
                    Actions.moveBy(0f, -50f, 0.5f)
                )
            )
        )
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("default", assets.fonts[FontOptions("roboto", 45f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
        })
    }
}
