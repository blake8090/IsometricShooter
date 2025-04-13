package bke.iso.editor2

import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World

abstract class EditorMode(val renderer: Renderer, val world: World) {
    abstract fun start()
    abstract fun stop()
    abstract fun update()
    abstract fun draw()
}
