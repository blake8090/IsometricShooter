package bke.iso.editorv2.ui

import bke.iso.editor.ui.color
import bke.iso.editorv2.actor.ActorTab
import bke.iso.editorv2.scene.SceneTab
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import io.github.oshai.kotlinlogging.KotlinLogging

private const val TABS_STYLE = "tabs"

class EditorScreen2(assets: Assets) : UIScreen(assets) {

    private val log = KotlinLogging.logger {}

    private val root = Table().top()

    private val sceneTab = SceneTab(skin, assets)
    private val actorTab = ActorTab(skin, assets)


    override fun create() {
        setup()

        sceneTab.create()
        actorTab.create()

        root.setFillParent(true)

        val menuBarStack = Stack()
        menuBarStack.add(sceneTab.menuBar)
        menuBarStack.add(actorTab.menuBar)
        root.add(menuBarStack).growX().left()

        root.row()
        root.add(createTabs())
            .growX()
            .top()
            .left()


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
    }

    private fun createTabs(): Table {
        val tabs = Table().left()
        tabs.background = skin.getDrawable("bg")

        val sceneButton = createButton("Scene")
        sceneButton.onChanged {
            sceneTab.menuBar.isVisible = true
            sceneTab.mainView.isVisible = true

            actorTab.menuBar.isVisible = false
            actorTab.mainView.isVisible = false
        }

        val actorButton = createButton("Actor")
        actorButton.onChanged {
            sceneTab.menuBar.isVisible = false
            sceneTab.mainView.isVisible = false

            actorTab.menuBar.isVisible = true
            actorTab.mainView.isVisible = true
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
