package bke.iso.editor.ui

import bke.iso.editor.ContextMenuSelection
import bke.iso.editor.EditorState
import bke.iso.editor.MainViewDragEvent
import bke.iso.editor.MainViewPressEvent
import bke.iso.editor.event.EditorEvent
import bke.iso.editor.event.EditorEventListener
import bke.iso.editor.ui.dialog.EditBuildingDialog
import bke.iso.editor.ui.dialog.EditTagsDialog
import bke.iso.editor.ui.dialog.NewBuildingDialog
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.render.makePixelTexture
import bke.iso.engine.ui.UIScreen
import bke.iso.engine.ui.util.newTintedDrawable
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle
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

    private val contextMenu = EditorContextMenu(skin)
    private var contextMenuActor: Actor? = null

    private lateinit var infoLabel: Label

    private val newBuildingDialog = NewBuildingDialog(skin)
    private val editBuildingDialog = EditBuildingDialog(skin)
    private val editTagsDialog = EditTagsDialog(skin)

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

    // TODO: use apply() in all places skin is being setup
    private fun setup() {
        skin.apply {
            add("pixel", makePixelTexture())
            add("bg", makePixelTexture(color(10, 23, 36)))

            add("default", assets.fonts[FontOptions("roboto.ttf", 13f, Color.WHITE)])

            add("default", Label.LabelStyle().apply {
                font = skin.getFont("default")
                background = skin.getDrawable("bg")
            })

            add("info", assets.fonts[FontOptions("roboto.ttf", 20f, Color.WHITE)])
            add("info", Label.LabelStyle().apply {
                font = skin.getFont("info")
            })

            add("button-up", color(20, 51, 82))
            add("button-over", color(34, 84, 133))
            add("button-down", color(43, 103, 161))
            add("button-checked", color(43, 103, 161))
            add("table-border", color(77, 100, 130))

            add("default", TextButton.TextButtonStyle().apply {
                font = skin.getFont("default")
                up = skin.newTintedDrawable("pixel", "button-up")
                down = skin.newTintedDrawable("pixel", "button-down")
                over = skin.newTintedDrawable("pixel", "button-over")
            })

            add("default", WindowStyle().apply {
                titleFont = skin.getFont("default")
                background = skin.getDrawable("bg")
            })
        }
    }

    private fun createMainView(): Table {
        val mainView = Table()
        mainView.touchable = Touchable.enabled

        mainView.add(toolBar.create())
            .expandX()
            .fillX()
            .top()
            .left()

        mainView.row()

        infoLabel = Label("", skin, "info")
        mainView.add(infoLabel)
            .pad(5f)
            .top()
            .left()

        mainView.row()

        val touchableArea = Table()
        touchableArea.name = MAIN_VIEW_NAME
        mainView.add(touchableArea).grow()

        mainView.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (hitMainView()) {
                    log.trace { "main view - touch down" }
                    mainView.fire(MainViewPressEvent())
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (hitMainView()) {
                    log.trace { "main view - touch up" }
                }
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                if (hitMainView()) {
                    log.trace { "main view - drag event" }
                    mainView.fire(MainViewDragEvent())
                }
            }
        })

        return mainView
    }

    fun hitMainView(): Boolean {
        val screenPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        val stagePos: Vector2 = stage.screenToStageCoordinates(screenPos)

        val actor = stage.hit(stagePos.x, stagePos.y, false) ?: return false
        log.trace { "hit actor ${actor::class.simpleName} ${actor.name}" }
        return actor.name == MAIN_VIEW_NAME
    }

    fun unselectPrefabs() {
        assetBrowser.unselectPrefabs()
    }

    fun updateLayerLabel(layer: Float) {
        toolBar.updateLayerLabel(layer)
    }

    fun openContextMenu(selections: Set<ContextMenuSelection>) {
        closeContextMenu()

        val screenPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        val stagePos: Vector2 = stage.screenToStageCoordinates(screenPos)

        contextMenuActor = contextMenu.create(stagePos.x, stagePos.y, selections)
        stage.addActor(contextMenuActor)
    }

    fun closeContextMenu() {
        contextMenuActor?.addAction(Actions.removeActor())
    }

    fun setInfoText(text: String) {
        infoLabel.setText(text)
    }

    fun openNewBuildingDialog(action: (String) -> Unit) {
        newBuildingDialog.create(stage, action)
    }

    fun openEditBuildingDialog(buildingNames: Set<String>, action: (String) -> Unit) {
        editBuildingDialog.create(stage, buildingNames, action)
    }

    fun openEditTagsDialog(actor: bke.iso.engine.world.actor.Actor) {
        editTagsDialog.create(stage, actor)
    }
}
