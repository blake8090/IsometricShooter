package bke.iso.editor

import bke.iso.editor.actor.ActorMode
import bke.iso.editor.scene.SceneMode
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module

class EditorModule(engine: Engine) : Module {

    private val sceneMode = SceneMode(engine)
    private val actorMode = ActorMode(engine)
    override val alwaysActive: Boolean = true

    private var selectedMode: EditorMode? = null

    override fun stop() {
        selectedMode?.stop()
        selectedMode = null
    }

    override fun update(deltaTime: Float) {
        selectedMode?.update()
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is ActorModeSelected -> selectMode(actorMode)
            is SceneModeSelected -> selectMode(sceneMode)
            is ExecuteCommand -> selectedMode?.execute(event.command)
            is EditorClosed -> stop()
            else -> selectedMode?.handleEvent(event)
        }
    }

    private fun selectMode(editorMode: EditorMode) {
        selectedMode?.stop()
        editorMode.start()
        selectedMode = editorMode
    }

    class ActorModeSelected : Event
    class SceneModeSelected : Event
    data class ExecuteCommand(val command: EditorCommand) : Event
    class EditorClosed : Event
}
