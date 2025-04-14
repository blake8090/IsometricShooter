package bke.iso.editor2

import bke.iso.editor2.actor.ActorPrefabMode
import bke.iso.editor2.scene.SceneMode
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
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

    private val sceneMode = SceneMode(engine)
    private val actorPrefabMode = ActorPrefabMode(engine.events, engine.assets)

    private var selectedMode: EditorMode? = null

    override suspend fun load() {
        log.info { "Starting editor" }

        engine.ui2.clear()
        engine.input.keyMouse.bindMouse("openContextMenu", Input.Buttons.RIGHT, ButtonState.RELEASED)

        selectMode(sceneMode)
    }

    private fun selectMode(editorMode: EditorMode) {
        selectedMode?.stop()
        editorMode.start()
        selectedMode = editorMode
    }

    override fun update(deltaTime: Float) {
        super.update(deltaTime)
        selectedMode?.update()
    }

    override fun onFrameEnd() {
        super.onFrameEnd()
        selectedMode?.draw()
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)

        when (event) {
            is ActorPrefabModeSelected -> selectMode(actorPrefabMode)
            is SceneModeSelected -> selectMode(sceneMode)
            else -> selectedMode?.handleEvent(event)
        }
    }

    class ActorPrefabModeSelected : Event

    class SceneModeSelected : Event
}
