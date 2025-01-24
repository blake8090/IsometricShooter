package bke.iso.editor.v3.scene

import bke.iso.engine.core.Event
import bke.iso.engine.ui.v2.UIViewController
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneTabViewController(view: SceneTabView) : UIViewController<SceneTabView>(view) {

    private val log = KotlinLogging.logger { }

    override fun start() {
        log.debug { "start" }
    }

    override fun stop() {
        log.debug { "stop" }
    }

    override fun update(deltaTime: Float) {
    }

    override fun handleEvent(event: Event) {
        log.debug { "handling event ${event::class.simpleName}" }
    }

    override fun handleEvent(event: com.badlogic.gdx.scenes.scene2d.Event) {
//        log.debug { "handling event ${event::class.simpleName}" }
    }
}
