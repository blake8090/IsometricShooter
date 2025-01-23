package bke.iso.editor.v2.actor

import bke.iso.editor.ui.color
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.UIElement
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class ActorTabView(skin: Skin, assets: Assets) : UIElement(skin) {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    private val componentInspectorElement = ComponentInspectorElement(skin, assets)
    private val componentBrowserElement = ComponentBrowserElement(skin)

    override fun create(): Actor {
        menuBar.background = skin.getDrawable("bg")

        menuBar.add(createMenuButton("New") {})
        menuBar.add(createMenuButton("Open") { button ->
            button.fire(ActorTabViewController.OpenPrefabEvent())
        })
        menuBar.add(createMenuButton("Save") {})
        menuBar.add(createMenuButton("Save As") {})

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

    private fun createMenuButton(text: String, action: (TextButton) -> Unit): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
            onChanged {
                action.invoke(this)
            }
        }
    }

    fun updateComponentBrowser(components: List<Component>) {
        componentBrowserElement.update(components)
    }

    fun updateComponentInspector(selectedComponent: Component) {
        componentInspectorElement.update(selectedComponent)
    }
}
