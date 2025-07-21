package bke.iso.editor

import bke.iso.editor.core.BaseEditor
import bke.iso.editor.core.EditorCommand
import bke.iso.editor.entity.EntityEditor
import bke.iso.editor.scene.SceneEditor
import bke.iso.engine.Engine
import bke.iso.engine.core.Event
import bke.iso.engine.core.Module
import io.github.oshai.kotlinlogging.KotlinLogging

class EditorModule(engine: Engine) : Module {

    private val log = KotlinLogging.logger { }

    override val alwaysActive: Boolean = true

    private val sceneEditor = SceneEditor(engine)
    private val entityEditor = EntityEditor(engine)

    private var selectedEditor: BaseEditor? = null

    override fun stop() {
        selectedEditor?.stop()
        selectedEditor = null
    }

    override fun update(deltaTime: Float) {
        selectedEditor?.update()
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is EntityEditorSelected -> startEditor(entityEditor)
            is SceneEditorSelected -> startEditor(sceneEditor)
            is ExecuteCommand -> {
                log.debug { "executing command ${event.command.name}" }
                selectedEditor?.executeCommand(event.command)
            }

            is EditorClosed -> stop()
            else -> selectedEditor?.handleEvent(event)
        }
    }

    private fun startEditor(editor: BaseEditor) {
        selectedEditor?.stop()
        editor.start()
        selectedEditor = editor
    }

    class EntityEditorSelected : Event
    class SceneEditorSelected : Event
    data class ExecuteCommand(val command: EditorCommand) : Event
    class EditorClosed : Event
}
