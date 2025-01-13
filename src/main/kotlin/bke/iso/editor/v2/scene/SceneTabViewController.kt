package bke.iso.editor.v2.scene

import bke.iso.editor.v2.core.EditorViewController
import bke.iso.engine.core.Module
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.oshai.kotlinlogging.KotlinLogging

class SceneTabViewController(
    skin: Skin,
) : EditorViewController<SceneTabView>() {

    private val log = KotlinLogging.logger { }

    override val modules: Set<Module> = emptySet()
    override val view: SceneTabView = SceneTabView(skin)

    override fun start() {
        log.debug { "Starting SceneTabViewController" }
    }

    override fun stop() {
        log.debug { "Stopping SceneTabViewController" }
    }
}
