package bke.iso.game.ui

import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.game.MainMenuState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import mu.KotlinLogging

class MainMenuScreen(
    private val assets: Assets,
    private val events: Game.Events
) : UIScreen() {

    private val log = KotlinLogging.logger {}

    override fun create() {
        setup()

        val root = Table()
        root.debug = true
        root.setFillParent(true)
        root.background = skin.newDrawable("white", Color.BLACK)
        stage.addActor(root)

        val stackTable = Table()
        stackTable.background = skin.newDrawable("white", Color.DARK_GRAY)
        stackTable.add(Label("ISOMETRIC SHOOTER", skin))
            .padLeft(50f)
            .padRight(50f)

        stackTable.row()
        val startButton = TextButton("START", skin)
        startButton.onChanged { _, _ ->
            log.debug { "Start clicked" }
            events.fire(MainMenuState.StartEvent())
        }
        controllerNavigation.add(startButton)
        stackTable.add(startButton)
            .padTop(50f)
            .padBottom(50f)

        stackTable.row()
        val quitButton = TextButton("QUIT", skin)
        quitButton.onChanged { _, _ ->
            log.debug { "Quit clicked" }
            Gdx.app.exit()
        }
        controllerNavigation.add(quitButton)
        stackTable.add(quitButton)
            .padTop(50f)
            .padBottom(50f)

        root.add(stackTable).center()
    }

    private fun setup() {
        skin.add("white", makePixelTexture())

        skin.add("title", assets.fonts[FontOptions("TitilliumWeb-SemiBold", 75f, Color.WHITE)])
        skin.add("button", assets.fonts[FontOptions("roboto", 65f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("title")
            background = skin.newDrawable("white", Color.DARK_GRAY)
        })

        skin.add("default", TextButtonStyle().apply {
            font = skin.getFont("button")
            up = skin.newDrawable("white", Color.LIGHT_GRAY)
            down = skin.newDrawable("white", Color.GRAY)
            over = skin.newDrawable("white", Color.BLUE)
        })
    }
}

fun TextButton.onChanged(action: (ChangeEvent, Actor) -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(event, actor)
        }
    })
}
