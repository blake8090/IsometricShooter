package bke.iso.editor.actor.ui

import bke.iso.editor.actor.OpenActorEvent
import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import kotlin.reflect.KClass

class ActorTabView(
    private val skin: Skin,
    private val stage: Stage,
    assets: Assets
) {

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
            .expandX()
            .growY()
            .minWidth(Value.percentWidth(0.175f, mainView))
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }

    fun openAddComponentDialog(componentTypes: List<KClass<out Component>>,  action: (KClass<out Component>) -> Unit) {
        SelectComponentTypeDialog2(skin)
            .create(stage, componentTypes, action)
    }
}
