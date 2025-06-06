package bke.iso.game.ui

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.ui.util.onEnter
import bke.iso.engine.ui.util.onExit
import bke.iso.engine.ui.scene2d.Scene2dView
import bke.iso.game.MainMenuState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.Event as Scene2dEvent

class MainMenuView(
    assets: Assets,
    private val events: Events
) : Scene2dView(assets) {

    override fun create() {
        setup()

        val root = Table()
        root.setFillParent(true)
        root.background = skin.newDrawable("pixel", Color.BLACK)
        stage.addActor(root)

        val stackTable = Table()
        stackTable.background = skin.newDrawable("pixel", Color.DARK_GRAY)
        stackTable.add(Label("ISOMETRIC SHOOTER", skin))
            .padLeft(50f)
            .padRight(50f)

        stackTable.row()
        val startButton = TextButton("START", skin).apply {
            onChanged {
                events.fire(MainMenuState.OnStartGame())
            }

            onEnter { button ->
                button.color = Color.BLUE
            }

            onExit { button ->
                button.color = Color.LIGHT_GRAY
            }
        }
        stackTable.add(startButton)
            .padTop(20f)
        controllerNavigation.add(startButton)

        stackTable.row()
        val editorButton = TextButton("EDITOR", skin).apply {
            onChanged {
                events.fire(MainMenuState.OnStartEditor())
            }

            onEnter { button ->
                button.color = Color.BLUE
            }

            onExit { button ->
                button.color = Color.LIGHT_GRAY
            }
        }
        stackTable.add(editorButton)
            .padTop(20f)
        controllerNavigation.add(editorButton)

        stackTable.row()
        val quitButton = TextButton("QUIT", skin).apply {
            onChanged { Gdx.app.exit() }

            onEnter { button ->
                button.color = Color.BLUE
            }

            onExit { button ->
                button.color = Color.LIGHT_GRAY
            }
        }
        stackTable.add(quitButton)
            .padTop(20f)
            .padBottom(20f)
        controllerNavigation.add(quitButton)

        root.add(stackTable).center()
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())

        skin.add("title", assets.fonts[FontOptions("TitilliumWeb-SemiBold.ttf", 30f, Color.WHITE)])
        skin.add("button", assets.fonts[FontOptions("roboto.ttf", 20f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("title")
            background = skin.newDrawable("pixel", Color.DARK_GRAY)
        })

        skin.add("default", TextButtonStyle().apply {
            font = skin.getFont("button")
            up = skin.newDrawable("pixel", Color.LIGHT_GRAY)
            down = skin.newDrawable("pixel", Color.GRAY)
            over = skin.newDrawable("pixel", Color.BLUE)
        })
    }

    override fun handleScene2dEvent(event: Scene2dEvent) {}

    override fun handleEvent(event: Event) {}
}
