package bke.iso.editor

import bke.iso.editor.ui.EditorScreen
import bke.iso.engine.Event
import bke.iso.engine.Module
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.World
import com.badlogic.gdx.graphics.Color

class BuildingsModule(
    private val world: World,
    private val renderer: Renderer,
    private val editorScreen: EditorScreen
) : Module {

    fun init() {
        editorScreen.setInfoText("Editing building 'test'")
    }

    override fun update(deltaTime: Float) {
        world.buildings.getBounds("test")?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.RED)
        }
    }

    override fun handleEvent(event: Event) {}
}
