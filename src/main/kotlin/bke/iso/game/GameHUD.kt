package bke.iso.game

import bke.iso.engine.asset.FontOptions
import bke.iso.engine.asset.Fonts
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle

class GameHUD(private val fonts: Fonts) : UIScreen() {

    private val skin = Skin()

    override fun create() {
        super.create()

        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        skin.add("white", Texture(pixmap))

        // TODO: reload fonts in skin when resizing?
        skin.add("default", fonts[FontOptions("roboto", 25f, Color.WHITE)])

        val textButtonStyle = TextButtonStyle().apply {
            up = skin.newDrawable("white", Color.DARK_GRAY)
            down = skin.newDrawable("white", Color.DARK_GRAY)
            checked = skin.newDrawable("white", Color.BLUE)
            over = skin.newDrawable("white", Color.LIGHT_GRAY)
            font = skin.getFont("default")
        }
        skin.add("default", textButtonStyle)
        skin.add("default", LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.newDrawable("white", Color.LIGHT_GRAY)
        })

        val table = Table()
        table.debug = true
        table.setFillParent(true)
        stage.addActor(table)

        val button = TextButton("click me!", skin)
        table.add(button)
        table.row()
        table.add(Label("test", skin)).height(100f).expand().left().bottom()
    }

    override fun dispose() {
        super.dispose()
        skin.dispose()
    }
}
