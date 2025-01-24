package bke.iso.editor.v3

import bke.iso.editor.v3.actor.ActorTabViewController
import bke.iso.editor.v3.actor.SelectNewComponentDialog
import bke.iso.editor.v3.scene.SceneTabViewController
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.ui.v2.UILayer
import bke.iso.engine.ui.v2.UIViewController
import bke.iso.engine.world.actor.Component
import kotlin.reflect.KClass

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
    private val sceneTabViewController = SceneTabViewController(editorView.sceneTabView, engine)
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

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        if (event is OnOpenSelectNewComponentDialog) {
            val dialog = SelectNewComponentDialog(skin, event.componentTypes, event.action)
            dialog.show(stage)
        }
    }

    data class OnOpenSelectNewComponentDialog(
        val componentTypes: List<KClass<out Component>>,
        val action: (KClass<out Component>) -> Unit
    ) : Event
}
