package bke.iso.editor

import bke.iso.editor.actor.ActorMode
import bke.iso.editor.scene.SceneMode
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System
import io.github.oshai.kotlinlogging.KotlinLogging

class ImGuiEditorState(override val engine: Engine) : State() {
    override val systems: LinkedHashSet<System> = LinkedHashSet()
    override val modules: Set<Module> = emptySet()

    private val log = KotlinLogging.logger {}

    private val sceneMode = SceneMode(engine)
    private val actorMode = ActorMode(engine)

    private var selectedMode: EditorMode? = null

    override suspend fun load() {
        log.info { "Starting editor" }

        engine.ui.clearScene2dViews()
        engine.ui.clearImGuiViews()

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
            is ActorPrefabModeSelected -> selectMode(actorMode)
            is SceneModeSelected -> selectMode(sceneMode)
            is ExecuteCommand -> selectedMode?.execute(event.command)
            else -> selectedMode?.handleEvent(event)
        }
    }

    class ActorPrefabModeSelected : Event

    class SceneModeSelected : Event

    data class ExecuteCommand(val command: EditorCommand) : Event
}
