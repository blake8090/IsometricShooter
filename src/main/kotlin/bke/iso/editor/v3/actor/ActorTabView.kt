package bke.iso.editor.v3.actor

import bke.iso.editor.ui.color
import bke.iso.editor.v2.actor.ActorTabViewController
import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.ui.v2.UIView
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class ActorTabView(private val skin: Skin, assets: Assets) : UIView() {

    val menuBar: Table = Table().left()
    val mainView: Table = BorderedTable(color(43, 103, 161))

    private val componentInspectorView = ComponentInspectorView(skin, assets)
    private val componentBrowserView = ComponentBrowserView(skin)

    override fun create() {
        menuBar.background = skin.getDrawable("bg")

        menuBar.add(createMenuButton("New") {})
        menuBar.add(createMenuButton("Open") { button ->
            button.fire(ActorTabViewController.OpenPrefabEvent())
        })
        menuBar.add(createMenuButton("Save") {})
        menuBar.add(createMenuButton("Save As") {})

        componentBrowserView.create()
        mainView.add(componentBrowserView.root)
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.1f, mainView))

        componentInspectorView.create()
        mainView.add(componentInspectorView.root)
            .top()
            .right()
            .expandX()
            .growY()
            .minWidth(Value.percentWidth(0.175f, mainView))
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
        componentBrowserView.update(components)
    }

    fun updateComponentInspector(selectedComponent: Component) {
        componentInspectorView.update(selectedComponent)
    }
}
