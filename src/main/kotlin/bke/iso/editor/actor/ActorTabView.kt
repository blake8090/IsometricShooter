package bke.iso.editor.actor

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.font.FontOptions
import bke.iso.engine.ui.util.BorderedTable
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

private const val ACTOR_TAB_STYLE = "actorTab"

class ActorTabView(
    private val skin: Skin,
    private val assets: Assets
) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    fun create() {
        setup()

        menuBar.background = skin.getDrawable("bg")
        menuBar.add(createMenuButton("New"))
        menuBar.add(createMenuButton("Open"))
        menuBar.add(createMenuButton("Save"))
        menuBar.add(createMenuButton("Save As"))
    }

    private fun setup() {
        skin.add(ACTOR_TAB_STYLE, TextButton.TextButtonStyle().apply {
            font = assets.fonts[FontOptions("roboto.ttf", 13f, Color.WHITE)]
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
//            checked = skin.newDrawable("pixel", color(43, 103, 161))
        })
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin, ACTOR_TAB_STYLE).apply {
            pad(5f)
        }
    }
}
