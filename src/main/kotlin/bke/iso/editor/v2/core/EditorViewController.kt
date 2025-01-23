package bke.iso.editor.v2.core

import bke.iso.engine.core.Module
import bke.iso.engine.ui.UIElement

abstract class EditorViewController<T : UIElement>(protected val view: T) : Module {

    abstract val modules: Set<Module>

    open fun handleEditorEvent(event: EditorEvent) {}
}
