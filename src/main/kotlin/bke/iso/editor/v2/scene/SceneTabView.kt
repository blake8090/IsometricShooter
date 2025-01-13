package bke.iso.editor.v2.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.UIElement
import bke.iso.engine.ui.util.onChanged
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class SceneTabView(skin: Skin, assets: Assets) : UIElement(skin) {

    val menuBar: Table = Table().left()
    val mainView: Table = Table()

    private val assetBrowserElement = AssetBrowserElement(skin, assets)
    private val toolbarElement = ToolbarElement(skin)
    private val sceneInspectorElement = SceneInspectorElement(skin)

    override fun create(): Actor {
        menuBar.background = skin.getDrawable("bg")
        // TODO: use same pattern in ActorTabView
        menuBar.add(createMenuButton("New") {})
        menuBar.add(createMenuButton("Open") {})
        menuBar.add(createMenuButton("Save") {})
        menuBar.add(createMenuButton("Save As") {})
        menuBar.add(createMenuButton("View") {})
        menuBar.add(createMenuButton("Buildings") {})

        mainView.add(assetBrowserElement.create())
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.1f, mainView))

        mainView.add(toolbarElement.create())
            .top()
            .left()
            .growX()

        mainView.add(sceneInspectorElement.create())
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.15f, mainView))

        return mainView
    }

    private fun createMenuButton(text: String, action: () -> Unit): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
            onChanged {
                action.invoke()
            }
        }
    }

    fun updateAssetBrowser(assets: List<Any>) {
        assetBrowserElement.update(assets)
    }
}
