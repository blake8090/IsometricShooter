package bke.iso.editor.v3.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.ui.v2.UIView
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value

class SceneTabView(private val skin: Skin, assets: Assets) : UIView() {

    val menuBar: Table = Table().left()
    val mainView: Table = Table()

    val assetBrowserView = AssetBrowserView(skin, assets)
    private val toolbarView = ToolbarView(skin, assets)
    private val sceneInspectorView = SceneInspectorView(skin)

    override fun create() {
        menuBar.background = skin.getDrawable("bg")

        menuBar.add(createMenuButton("New") {})
        menuBar.add(createMenuButton("Open") {})
        menuBar.add(createMenuButton("Save") {})
        menuBar.add(createMenuButton("Save As") {})
        menuBar.add(createMenuButton("View") {})
        menuBar.add(createMenuButton("Buildings") {})

        assetBrowserView.create()
        mainView.add(assetBrowserView.root)
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.125f, mainView))

        toolbarView.create()
        mainView.add(toolbarView.root)
            .top()
            .left()
            .growX()

        sceneInspectorView.create()
        mainView.add(sceneInspectorView.root)
            .top()
            .left()
            .growY()
            .minWidth(Value.percentWidth(0.15f, mainView))
    }

    private fun createMenuButton(text: String, action: (TextButton) -> Unit): TextButton {
        return TextButton(text, skin).apply {
            pad(5f)
            onChanged {
                action.invoke(this)
            }
        }
    }
}
