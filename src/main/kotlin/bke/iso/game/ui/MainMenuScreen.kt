package bke.iso.game.ui

import bke.iso.engine.Game
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.game.MainMenuState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle

class MainMenuScreen(
    private val assets: Assets,
    private val events: Game.Events
) : UIScreen() {

    override fun create() {
        setup()

        val root = Table()
        root.debug = true
        root.setFillParent(true)
        root.background = skin.newDrawable("white", Color.BLACK)
        stage.addActor(root)

        val stackTable = Table()
        stackTable.debug = true
        stackTable.background = skin.newDrawable("white", Color.DARK_GRAY)
        stackTable.add(Label("ISOMETRIC SHOOTER", skin))
            .padLeft(50f)
            .padRight(50f)

        stackTable.row()
        val startButton = TextButtonBuilder("START", skin)
            .onChanged { _, _ ->
                events.fire(MainMenuState.StartEvent())
            }
            .onEnter { _, actor ->
                actor.color = Color.BLUE
            }
            .onExit { _, actor ->
                actor.color = Color.LIGHT_GRAY
            }
            .build()
        stackTable.add(startButton)
            .padTop(20f)
        controllerNavigation.add(startButton)

        stackTable.row()
        val quitButton = TextButtonBuilder("QUIT", skin)
            .onChanged { _, _ ->
                Gdx.app.exit()
            }
            .onEnter { _, actor ->
                actor.color = Color.BLUE
            }
            .onExit { _, actor ->
                actor.color = Color.LIGHT_GRAY
            }
            .build()
        stackTable.add(quitButton)
            .padTop(20f)
            .padBottom(20f)
        controllerNavigation.add(quitButton)

        root.add(stackTable).center()
    }

    private fun setup() {
        skin.add("white", makePixelTexture())

        skin.add("title", assets.fonts[FontOptions("ui/TitilliumWeb-SemiBold", 75f, Color.WHITE)])
        skin.add("button", assets.fonts[FontOptions("ui/roboto", 65f, Color.WHITE)])

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
