package bke.iso.editor.scene.command

import bke.iso.editor.core.command.EditorCommand
import bke.iso.engine.lighting.Lighting
import com.badlogic.gdx.graphics.Color

class SetAmbientLightCommand(
    private val lighting: Lighting,
    private val color: Color
) : EditorCommand() {

    override val name = "SetAmbientLight"

    private lateinit var previousColor: Color

    override fun execute() {
        previousColor = lighting.ambientLight
        lighting.ambientLight = color
    }

    override fun undo() {
        lighting.ambientLight = previousColor
    }
}
