package bke.iso.game.ui

import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.ui.UIScreen
import bke.iso.game.MainMenuState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import mu.KotlinLogging

class MainMenuScreen(
    private val assets: Assets,
    private val events: Game.Events
) : UIScreen() {

    private val log = KotlinLogging.logger {}

    override fun create() {
        super.create()
        setup()

        val root = Table()
        root.debug = true
        root.setFillParent(true)
        root.background = skin.newDrawable("white", Color.BLACK)
        stage.addActor(root)

        val stackTable = Table()
        stackTable.background = skin.newDrawable("white", Color.DARK_GRAY)
        stackTable.add(Label("Isometric Shooter", skin))
            .padLeft(50f)
            .padRight(50f)

        stackTable.row()
        stackTable.add(TextButton("Start", skin).apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    log.debug { "Start clicked" }
                    events.fire(MainMenuState.StartEvent())
                }
            })
        })
            .width(700f)
            .padTop(50f)
            .padBottom(50f)

        stackTable.row()
        stackTable.add(TextButton("Quit", skin).apply {
            addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    log.debug { "Quit clicked" }
                    Gdx.app.exit()
                }
            })
        })
            .width(700f)
            .padTop(50f)
            .padBottom(50f)
        root.add(stackTable).center()
    }

    private fun setup() {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        skin.add("white", Texture(pixmap))

        skin.add("title", assets.fonts[FontOptions("roboto", 75f, Color.WHITE)])
        skin.add("button", assets.fonts[FontOptions("roboto", 65f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("title")
            background = skin.newDrawable("white", Color.DARK_GRAY)
        })

        skin.add("default", TextButtonStyle().apply {
            up = skin.newDrawable("white", Color.LIGHT_GRAY)
            down = skin.newDrawable("white", Color.GRAY)
            checked = skin.newDrawable("white", Color.LIGHT_GRAY)
            over = skin.newDrawable("white", Color.BLUE)
            font = skin.getFont("button")
        })
    }
}
