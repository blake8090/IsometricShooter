package bke.iso.editorv2.scene.ui

import bke.iso.editor.ui.EditorAssetBrowser
import bke.iso.editor.ui.EditorToolBar
import bke.iso.editor.ui.color
import bke.iso.editorv2.scene.OpenSceneEvent
import bke.iso.editorv2.scene.SaveSceneEvent
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

private const val SCENE_TAB_STYLE = "sceneTab"

class EditorSceneTab(
    private val skin: Skin,
    private val assets: Assets
) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    private val assetBrowser = EditorAssetBrowser(skin, assets)
    private val editorToolBar = EditorToolBar(skin, assets)
    private val sceneInspector = EditorSceneInspector(skin, assets)

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
            }
        })

        mainView.row()
        mainView.add(assetBrowser.create())

        mainView.add(editorToolBar.create())
            .growX()
            .top()

        mainView.add(sceneInspector.create())
            .growY()
    }

    private fun setup() {
        skin.add(SCENE_TAB_STYLE, TextButton.TextButtonStyle().apply {
            font = assets.fonts[FontOptions("roboto.ttf", 13f, Color.WHITE)]
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
//            checked = skin.newDrawable("pixel", color(43, 103, 161))
        })
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin, SCENE_TAB_STYLE).apply {
            pad(5f)
        }
    }
}
