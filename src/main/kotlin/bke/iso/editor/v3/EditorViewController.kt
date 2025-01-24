package bke.iso.editor.v3

import bke.iso.editor.v3.actor.ActorTabViewController
import bke.iso.editor.v3.scene.SceneTabViewController
import bke.iso.engine.ui.v2.UIViewController
import io.github.oshai.kotlinlogging.KotlinLogging
import com.badlogic.gdx.scenes.scene2d.Event as GdxEvent

class EditorViewController(
    view: EditorView,
    private val sceneTabViewController: SceneTabViewController,
    private val actorTabViewController: ActorTabViewController
) : UIViewController<EditorView>(view) {

    private val log = KotlinLogging.logger { }

    override fun start() {
        log.debug { "start" }
    }

    override fun stop() {
        log.debug { "stop" }
    }

    override fun handleEvent(event: GdxEvent) {
        if (event is EditorView.OnSelectTab) {
            log.debug { "selected tab ${event.tab}" }
            when (event.tab) {
                Tab.SCENE -> {
                    sceneTabViewController.enabled = true
                    actorTabViewController.enabled = false
                }

                Tab.ACTOR -> {
                    sceneTabViewController.enabled = false
                    actorTabViewController.enabled = true
                }

                Tab.NONE -> {
                    log.warn { "No tab selected!" }
                }
            }
        }
    }
}
