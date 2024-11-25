package bke.iso.editorv2.ui

import bke.iso.editorv2.EditorEvent
import bke.iso.editorv2.EditorEventListener
import bke.iso.editorv2.EditorState2
import bke.iso.editorv2.actor.ActorTabView
import bke.iso.editorv2.scene.ui.SceneTabView
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import io.github.oshai.kotlinlogging.KotlinLogging

private const val TABS_STYLE = "tabs"

class EditorScreen(
    private val editorState: EditorState2,
    assets: Assets
) : UIScreen(assets) {

    private val log = KotlinLogging.logger {}

    private val root = Table().top()

    val sceneTabView = SceneTabView(skin, assets, stage)
    val actorTabView = ActorTabView(skin, assets)

    override fun create() {
        setup()

        sceneTabView.create()
        actorTabView.create()

        root.setFillParent(true)

        val menuBarStack = Stack()
        menuBarStack.add(sceneTabView.menuBar)
        menuBarStack.add(actorTabView.menuBar)
        root.add(menuBarStack)
            .growX()
            .left()

        root.row()
        root.add(createTabs())
            .growX()
            .top()
            .left()

        root.row()

        val mainViewStack = Stack()
        mainViewStack.add(sceneTabView.mainView)
        mainViewStack.add(actorTabView.mainView)
        root.add(mainViewStack).grow()

        root.addListener(object : EditorEventListener {
            override fun handle(event: EditorEvent) {
                editorState.handleEvent(event)
            }
        })

        stage.addActor(root)
    }

    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))

        skin.add(TABS_STYLE, TextButton.TextButtonStyle().apply {
            font = assets.fonts[FontOptions("roboto.ttf", 14f, Color.WHITE)]
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
            checked = skin.newDrawable("pixel", color(43, 103, 161))
        })

        setupDefaultStyle()
    }

    private fun setupDefaultStyle() {
        skin.add("default", assets.fonts[FontOptions("roboto.ttf", 13f, Color.WHITE)])

        skin.add("default", Label.LabelStyle().apply {
            font = skin.getFont("default")
            background = skin.getDrawable("bg")
        })

        skin.add("button-up", color(20, 51, 82))
        skin.add("button-over", color(34, 84, 133))
        skin.add("button-down", color(43, 103, 161))
        skin.add("button-checked", color(43, 103, 161))
        skin.add("table-border", color(77, 100, 130))
    }

    private fun createTabs(): Table {
        val tabs = Table().left()
        tabs.background = skin.getDrawable("bg")

        val sceneButton = createButton("Scene")
        sceneButton.onChanged {
            sceneTabView.menuBar.isVisible = true
            sceneTabView.mainView.isVisible = true

            actorTabView.menuBar.isVisible = false
            actorTabView.mainView.isVisible = false
        }

        val actorButton = createButton("Actor")
        actorButton.onChanged {
            sceneTabView.menuBar.isVisible = false
            sceneTabView.mainView.isVisible = false

            actorTabView.menuBar.isVisible = true
            actorTabView.mainView.isVisible = true
        }

        tabs.add(sceneButton)
        tabs.add(actorButton)

        ButtonGroup<TextButton>().add(sceneButton, actorButton)

        return tabs
    }

    private fun createButton(text: String): TextButton {
        val vPad = 10f
        val hPad = 10f
        return TextButton(text, skin, TABS_STYLE).apply {
            padTop(vPad)
            padBottom(vPad)
            padLeft(hPad)
            padRight(hPad)
        }
    }
}
