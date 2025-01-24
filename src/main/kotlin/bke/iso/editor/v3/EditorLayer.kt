package bke.iso.editor.v3

import bke.iso.engine.Engine
import bke.iso.engine.ui.v2.UILayer
import bke.iso.engine.ui.v2.UIViewController

class EditorLayer(engine: Engine) : UILayer(engine) {

    override val controllers: Set<UIViewController<*>> = emptySet()

    private val editorView = EditorView(skin, engine.assets)

    override fun create() {
        editorView.create()
        stage.addActor(editorView.root)
    }
}
