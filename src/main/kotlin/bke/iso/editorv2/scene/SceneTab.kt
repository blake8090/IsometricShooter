package bke.iso.editorv2.scene

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

private const val SCENE_TAB_STYLE = "sceneTab"

class SceneTab(
    private val skin: Skin,
    private val assets: Assets
) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    fun create() {
        setup()

        menuBar.add(TextButton("New", skin, SCENE_TAB_STYLE))
        menuBar.add(TextButton("Open", skin, SCENE_TAB_STYLE))
        menuBar.add(TextButton("Save", skin, SCENE_TAB_STYLE))
        menuBar.add(TextButton("Save As", skin, SCENE_TAB_STYLE))
        menuBar.add(TextButton("View", skin, SCENE_TAB_STYLE))
    }

    private fun setup() {
        skin.add(SCENE_TAB_STYLE, TextButton.TextButtonStyle().apply {
            font = assets.fonts[FontOptions("roboto.ttf", 13f, Color.WHITE)]
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
            checked = skin.newDrawable("pixel", color(43, 103, 161))
        })
    }
}
