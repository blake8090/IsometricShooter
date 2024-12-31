package bke.iso.editor.actor.ui

import bke.iso.editor.actor.OpenActorEvent
import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class ActorTabView(private val skin: Skin, assets: Assets) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    val actorInspectorView = ActorInspectorView(skin, assets)
    val actorComponentBrowserView = ActorComponentBrowserView(skin)

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

        mainView.add(actorComponentBrowserView.create())
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.1f, mainView))

        mainView.add(actorInspectorView.create())
            .top()
            .right()
            .expand()
            .growY()
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }
}
