package bke.iso.editor.scene.ui

import bke.iso.editor.scene.MainViewDragEvent
import bke.iso.editor.scene.MainViewPressEvent
import bke.iso.editor.scene.OpenBuildingsMenuEvent
import bke.iso.editor.scene.OpenSceneEvent
import bke.iso.editor.scene.OpenViewMenuEvent
import bke.iso.editor.scene.SaveSceneEvent
import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import io.github.oshai.kotlinlogging.KotlinLogging

private const val TOUCHABLE_AREA_NAME = "touchableArea"

class SceneTabView(
    private val skin: Skin,
    private val assets: Assets,
    private val stage: Stage
) {

    private val log = KotlinLogging.logger { }

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    private val assetBrowser = SceneAssetBrowserView(skin, assets)
    private val toolBarView = SceneToolbarView(skin, assets)
    val sceneInspectorView = SceneInspectorView(skin)

    private val newBuildingDialog = NewBuildingDialog(skin)
    private val editBuildingDialog = EditBuildingDialog(skin)

    private lateinit var infoLabel: Label

    fun create() {
        setup()

        menuBar.background = skin.getDrawable("bg")

        menuBar.add(createMenuButton("New").apply {
            onChanged {
            }
        })
        menuBar.add(createMenuButton("Open").apply {
            onChanged {
                fire(OpenSceneEvent())
            }
        })
        menuBar.add(createMenuButton("Save").apply {
            onChanged {
                fire(SaveSceneEvent())
            }
        })
        menuBar.add(createMenuButton("Save As").apply {
            onChanged {
                fire(SaveSceneEvent())
            }
        })
        menuBar.add(createMenuButton("View").apply {
            onChanged {
                fire(OpenViewMenuEvent(Vector2(this.x, this.y + this.height)))
            }
        })
        menuBar.add(createMenuButton("Buildings").apply {
            onChanged {
                fire(OpenBuildingsMenuEvent(Vector2(this.x, this.y + this.height)))
            }
        })

        mainView.row()
        mainView.add(assetBrowser.create())

        mainView.add(createMainViewArea())
            .grow()

        mainView.add(sceneInspectorView.create())
            .growY()

        mainView.addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (hitTouchableArea()) {
                    log.trace { "main view - touch down" }
                    mainView.fire(MainViewPressEvent())
                }
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                if (hitTouchableArea()) {
                    log.trace { "main view - touch up" }
                }
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                if (hitTouchableArea()) {
                    log.trace { "main view - drag event" }
                    mainView.fire(MainViewDragEvent())
                }
            }
        })
    }

    private fun setup() {
        skin.add("sceneTabInfo", LabelStyle().apply {
            font = assets.fonts[FontOptions("roboto.ttf", 20f, Color.WHITE)]
        })
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }

    private fun createMainViewArea(): Table {
        val area = Table().top()
        area.touchable = Touchable.enabled

        area.add(toolBarView.create())
            .growX()

        area.row()
        infoLabel = Label("", skin, "sceneTabInfo")
        area.add(infoLabel)
            .left()
            .padLeft(5f)

        area.row()
        val touchableArea = Table()
        touchableArea.name = TOUCHABLE_AREA_NAME
        area.add(touchableArea)
            .grow()

        return area
    }

    fun hitTouchableArea(): Boolean {
        val screenPos = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        val stagePos: Vector2 = stage.screenToStageCoordinates(screenPos)

        val actor = stage.hit(stagePos.x, stagePos.y, false) ?: return false
        log.trace { "hit actor ${actor::class.simpleName} ${actor.name}" }
        return actor.name == TOUCHABLE_AREA_NAME
    }

    fun updateLayerLabel(layer: Float) {
        toolBarView.updateLayerLabel(layer)
    }

    fun updateInfoLabel(text: String) {
        infoLabel.setText(text)
    }

    fun unselectPrefabs() {
        assetBrowser.unselectPrefabs()
    }

    fun openNewBuildingDialog(action: (String) -> Unit) {
        newBuildingDialog.create(stage, action)
    }

    fun openEditBuildingDialog(buildingNames: Set<String>, action: (String) -> Unit) {
        editBuildingDialog.create(stage, buildingNames, action)
    }
}
