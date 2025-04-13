package bke.iso.editor2

import bke.iso.editor2.scene.SceneEditorMode
import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.input.ButtonState
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import com.badlogic.gdx.Input
import io.github.oshai.kotlinlogging.KotlinLogging

class ImGuiEditorState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = LinkedHashSet()
    override val modules: Set<Module> = emptySet()

    private val log = KotlinLogging.logger {}

    var selectedLayer = 0f
        private set

    var selectedMode = SceneEditorMode(engine.renderer, engine.world, engine.assets)

    override suspend fun load() {
        log.info { "Starting editor" }

        engine.ui2.stop()
        engine.input.keyMouse.bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)
    }

    override fun onFrameEnd() {
        super.onFrameEnd()
        selectedMode.draw()
    }
}
