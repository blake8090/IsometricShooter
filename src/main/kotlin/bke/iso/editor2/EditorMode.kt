package bke.iso.editor2

import bke.iso.engine.core.Event
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World

abstract class EditorMode(protected val renderer: Renderer, protected val world: World) {
    abstract fun start()
    abstract fun stop()
    abstract fun update()
    abstract fun draw()
    abstract fun handleEvent(event: Event)
}
