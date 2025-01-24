package bke.iso.editor.v3

import bke.iso.engine.Engine
import bke.iso.engine.core.Module
import bke.iso.engine.state.State
import bke.iso.engine.state.System

class EditorState3(override val engine: Engine) : State() {

    override val systems: LinkedHashSet<System> = linkedSetOf()
    override val modules: Set<Module> = emptySet()

    override suspend fun load() {
        engine.ui2.setLayer(EditorLayer(engine))
    }

//    override fun update(deltaTime: Float) {
//        super.update(deltaTime)
//    }
//
//    override fun handleEvent(event: Event) {
//        super.handleEvent(event)
//    }
}
