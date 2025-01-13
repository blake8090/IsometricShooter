package bke.iso.editor.v2.scene

import bke.iso.editor.ui.color
import bke.iso.engine.ui.UIElement
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton

class SceneTabView(skin: Skin) : UIElement(skin) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    override fun create(): Actor {
        menuBar.background = skin.getDrawable("bg")
        menuBar.add(createMenuButton("New").apply {
            onChanged {
            }
        })
        menuBar.add(createMenuButton("Open").apply {
            onChanged {
//                fire(OpenSceneEvent())
            }
        })
        menuBar.add(createMenuButton("Save").apply {
            onChanged {
//                fire(SaveSceneEvent())
            }
        })
        menuBar.add(createMenuButton("Save As").apply {
            onChanged {
//                fire(SaveSceneEvent())
            }
        })
        menuBar.add(createMenuButton("View").apply {
            onChanged {
//                fire(OpenViewMenuEvent(Vector2(this.x, this.y + this.height)))
            }
        })
        menuBar.add(createMenuButton("Buildings").apply {
            onChanged {
//                fire(OpenBuildingsMenuEvent(Vector2(this.x, this.y + this.height)))
            }
        })

        return mainView
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }
}
