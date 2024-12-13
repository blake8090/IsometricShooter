package bke.iso.editor.actor

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.render.Sprite
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class ActorTabView(
    private val skin: Skin,
    private val assets: Assets,
    private val stage: Stage
) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    fun create() {
        menuBar.background = skin.getDrawable("bg")
        menuBar.add(createMenuButton("New"))
        menuBar.add(createMenuButton("Open").apply {
            onChanged {
                fire(OpenActorEvent())
            }
        })
        menuBar.add(createMenuButton("Save"))
        menuBar.add(createMenuButton("Save As"))
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }

    fun draw(sprite: Sprite) {
        val tex = assets.get<Texture>(sprite.texture)
        stage.batch.begin()
        stage.batch.draw(tex, 0f, 0f)
        stage.batch.end()
    }
}
