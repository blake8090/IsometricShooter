package bke.iso.editor.v2.core

import bke.iso.engine.core.Module
import bke.iso.engine.ui.UIElement

abstract class EditorViewController<T : UIElement> : Module {

    abstract val modules: Set<Module>

    abstract val view: T

    open fun handleEditorEvent(event: EditorEvent) {}
}
