package bke.iso.editorv2.ui

import bke.iso.editor.event.EditorEvent
import bke.iso.editor.event.EditorEventListener
import bke.iso.editor.ui.color
import bke.iso.editorv2.EditorState2
import bke.iso.editorv2.actor.EditorActorTab
import bke.iso.editorv2.scene.ui.EditorSceneTab
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

class EditorScreen2(
    private val editorState: EditorState2,
    assets: Assets
) : UIScreen(assets) {

    private val log = KotlinLogging.logger {}

    private val root = Table().top()

    private val editorSceneTab = EditorSceneTab(skin, assets, stage)
    private val editorActorTab = EditorActorTab(skin, assets)


    override fun create() {
        setup()

        editorSceneTab.create()
        editorActorTab.create()

        root.setFillParent(true)

        val menuBarStack = Stack()
        menuBarStack.add(editorSceneTab.menuBar)
        menuBarStack.add(editorActorTab.menuBar)
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
        mainViewStack.add(editorSceneTab.mainView)
        mainViewStack.add(editorActorTab.mainView)
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
            editorSceneTab.menuBar.isVisible = true
            editorSceneTab.mainView.isVisible = true

            editorActorTab.menuBar.isVisible = false
            editorActorTab.mainView.isVisible = false
        }

        val actorButton = createButton("Actor")
        actorButton.onChanged {
            editorSceneTab.menuBar.isVisible = false
            editorSceneTab.mainView.isVisible = false

            editorActorTab.menuBar.isVisible = true
            editorActorTab.mainView.isVisible = true
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
