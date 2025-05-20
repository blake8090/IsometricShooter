package bke.iso.editor.entity.command

import bke.iso.editor.core.EditorCommand
import com.badlogic.gdx.math.Vector3

data class UpdateVector3Command(
    val vector3: Vector3,
    val x: Float,
    val y: Float,
    val z: Float
) : EditorCommand() {

    override val name = "UpdateVector3Command"

    private var previousX = vector3.x
    private var previousY = vector3.y
    private var previousZ = vector3.z

    override fun execute() {
        vector3.set(x, y, z)
    }

    override fun undo() {
        vector3.set(previousX, previousY, previousZ)
    }
}
