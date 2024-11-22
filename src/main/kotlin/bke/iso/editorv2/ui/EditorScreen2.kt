package bke.iso.editorv2.ui

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import io.github.oshai.kotlinlogging.KotlinLogging

private const val TABS_STYLE = "tabs"

class EditorScreen2(assets: Assets) : UIScreen(assets) {

    private val log = KotlinLogging.logger {}

    private val root = Table()

    override fun create() {
        setup()

        root.apply {
            setFillParent(true)

            add(createTabs())
                .top()
                .left()
                .growX()

            row()

            add(createMainView())
                .grow()
        }

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

        val sceneTab = createTab("Scene")
        val actorTab = createTab("Actor")

        tabs.add(sceneTab)
        tabs.add(actorTab)

        ButtonGroup<TextButton>().add(sceneTab, actorTab)

        return tabs
    }

    private fun createTab(text: String): TextButton {
        val vPad = 10f
        val hPad = 10f
        return TextButton(text, skin, TABS_STYLE).apply {
            padTop(vPad)
            padBottom(vPad)
            padLeft(hPad)
            padRight(hPad)
        }
    }

    private fun createMainView(): Table {
        val mainView = Table().left()
        return mainView
    }
}
