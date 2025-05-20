package bke.iso.editor.scene.tool

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.editor.scene.command.PaintRoomCommand
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.floor
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment

class RoomTool(
    override val collisions: Collisions,
    private val renderer: Renderer,
    private val worldLogic: WorldLogic
) : BaseTool() {

    private var dragging = false
    private val start = Vector3()
    private val end = Vector3()

    var selectedTemplate: EntityTemplate? = null

    override fun update() {
        val pos = Vector3(pointerPos).floor()

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!dragging) {
                startDragging(pos)
            }
        } else if (dragging) {
            stopDragging(pos)
        }
    }

    override fun draw() {
        if (!dragging) {
            return
        }

        val pos = Vector3(pointerPos).floor()
        renderer.fgShapes.addBox(Box.fromMinMax(start, pos), 1f, Color.RED)
    }

    private fun startDragging(pointerPos: Vector3) {
        dragging = true
        start.set(pointerPos)
    }

    private fun stopDragging(pointerPos: Vector3) {
        dragging = false
        end.set(pointerPos)
    }

    override fun performAction(): EditorCommand? = null

    override fun performMultiAction(): EditorCommand? = null

    override fun performReleaseAction(): EditorCommand? {
        val template = selectedTemplate ?: return null
        val box = Box.fromMinMax(Segment(start, end))
        return PaintRoomCommand(worldLogic, template, box)
    }
}
