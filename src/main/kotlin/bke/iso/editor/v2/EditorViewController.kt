package bke.iso.editor.v2

import bke.iso.editor.EditorEvent
import bke.iso.engine.core.Module
import bke.iso.engine.ui.UIComponent

abstract class EditorViewController<T : UIComponent> : Module {

    abstract val modules: Set<Module>

    abstract val view: T

    abstract fun handleEditorEvent(event: EditorEvent)
}
