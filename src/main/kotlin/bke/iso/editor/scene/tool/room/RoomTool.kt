package bke.iso.editor.scene.tool.room

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.ReferenceActorModule
import bke.iso.editor.scene.tool.SceneTabTool
import bke.iso.engine.asset.prefab.ActorPrefab
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
    private val referenceActorModule: ReferenceActorModule
) : SceneTabTool() {

    private var dragging = false
    private val start = Vector3()
    private val end = Vector3()

    var selectedPrefab: ActorPrefab? = null

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
        val prefab = selectedPrefab ?: return null
        val box = Box.fromMinMax(Segment(start, end))
        return PaintRoomCommand(referenceActorModule, prefab, box)
    }
}
