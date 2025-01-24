package bke.iso.editor.v3

import bke.iso.editor.v3.actor.ActorTabViewController
import bke.iso.editor.v3.scene.SceneTabViewController
import bke.iso.engine.Engine
import bke.iso.engine.ui.v2.UILayer
import bke.iso.engine.ui.v2.UIViewController

class EditorLayer(engine: Engine) : UILayer(engine) {

    private val editorView = EditorView(skin, engine.assets)

    private val actorTabViewController =
        ActorTabViewController(
            editorView.actorTabView,
            engine.assets,
            engine.events,
            engine.input,
            engine.rendererManager,
            engine.dialogs,
            engine.serializer
        )
    private val sceneTabViewController =
        SceneTabViewController(
            editorView.sceneTabView,
            engine.assets,
            engine.renderer,
            engine.rendererManager,
            engine.input
        )
    private val editorViewController =
        EditorViewController(
            editorView,
            sceneTabViewController,
            actorTabViewController
        )

    override val controllers: Set<UIViewController<*>> =
        setOf(actorTabViewController, sceneTabViewController, editorViewController)

    override fun create() {
        editorView.create()
        stage.addActor(editorView.root)
    }
}
