package bke.iso.editor.v2.actor

import bke.iso.editor.actor.OpenActorPrefabEvent
import bke.iso.editor.actor.SaveActorPrefabEvent
import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.UIComponent
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import kotlin.reflect.KClass

class ActorTabView(
    private val skin: Skin,
    assets: Assets
) : UIComponent(skin) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    private val componentInspectorElement = ComponentInspectorElement(skin, assets)
    private val componentBrowserElement = ComponentBrowserElement(skin)

    override fun create(): Actor {
        menuBar.background = skin.getDrawable("bg")
        menuBar.add(createMenuButton("New"))
        menuBar.add(createMenuButton("Open").apply {
            onChanged {
                fire(OpenActorPrefabEvent())
            }
        })
        menuBar.add(createMenuButton("Save").apply {
            onChanged {
                fire(SaveActorPrefabEvent())
            }
        })
        menuBar.add(createMenuButton("Save As"))

        mainView.add(componentBrowserElement.create())
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.1f, mainView))

        mainView.add(componentInspectorElement.create())
            .top()
            .right()
            .expandX()
            .growY()
            .minWidth(Value.percentWidth(0.175f, mainView))
        return mainView
    }

    private fun createMenuButton(text: String): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
        }
    }

//    fun openAddComponentDialog(componentTypes: List<KClass<out Component>>, action: (KClass<out Component>) -> Unit) {
//        SelectComponentTypeDialog2(skin)
//            .create(stage, componentTypes, action)
//    }
}
