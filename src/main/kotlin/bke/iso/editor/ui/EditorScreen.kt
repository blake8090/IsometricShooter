package bke.iso.editor.ui

import bke.iso.editor.EditorEvent
import bke.iso.editor.EditorEventListener
import bke.iso.editor.EditorState
import bke.iso.editor.ui.browser.EditorAssetBrowser
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import mu.KotlinLogging

private const val MAIN_VIEW_NAME = "mainView"

class EditorScreen(
    private val editorState: EditorState,
    assets: Assets
) : UIScreen(assets) {

    private val log = KotlinLogging.logger {}

    private val root = Table()
    private val menuBar = EditorMenuBar(skin)
    private val toolBar = EditorToolBar(skin, assets)
    private val assetBrowser = EditorAssetBrowser(skin, assets)

    override fun create() {
        setup()

        root.apply {
            setFillParent(true)

            add(menuBar.create())
                .growX()
                .colspan(2)
            row()
            add(assetBrowser.create())
                .growY()
                .top()
                .left()
            add(createMainView())
                .grow()

            addListener(object : EditorEventListener {
                override fun handle(event: EditorEvent) =
                    editorState.handleEvent(event)
            })
        }

        stage.addActor(root)
    }

    fun hitMainView(): Boolean {
        val screenPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        val stagePos: Vector2 = stage.screenToStageCoordinates(screenPos)

        val actor = stage.hit(stagePos.x, stagePos.y, false) ?: return false
        log.trace { "hit actor ${actor::class.simpleName} ${actor.name}" }
        return actor.name == MAIN_VIEW_NAME
    }

    private fun createMainView(): Table {
        val mainView = Table()
        mainView.name = MAIN_VIEW_NAME
        mainView.add(toolBar.create())
            .expand()
            .fillX()
            .top()
            .left()
        return mainView
    }

    // TODO: use apply() in all places skin is being setup
    private fun setup() {
        skin.add("pixel", makePixelTexture())
        skin.add("bg", makePixelTexture(color(10, 23, 36)))

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

        skin.add("default", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            up = skin.newTintedDrawable("pixel", "button-up")
            down = skin.newTintedDrawable("pixel", "button-down")
            over = skin.newTintedDrawable("pixel", "button-over")
        })
    }
}