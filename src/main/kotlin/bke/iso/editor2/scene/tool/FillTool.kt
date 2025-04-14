package bke.iso.editor2.scene.tool

import bke.iso.editor2.EditorCommand
import bke.iso.editor2.scene.WorldLogic
import bke.iso.editor2.scene.command.FillActorCommand
import bke.iso.editor2.scene.command.FillTileCommand
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collisions
import bke.iso.engine.math.Box
import bke.iso.engine.math.floor
import bke.iso.engine.render.Renderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Segment
import io.github.oshai.kotlinlogging.KotlinLogging

class FillTool(
    override val collisions: Collisions,
    private val renderer: Renderer,
    private val worldLogic: WorldLogic
) : BaseTool() {

    private val log = KotlinLogging.logger {}

    private var dragging = false
    private val start = Vector3()
    private val end = Vector3()

    private var selection: Selection? = null

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
        if (dragging) {
            val pos = Vector3(pointerPos).floor()
            renderer.fgShapes.addBox(Box.fromMinMax(start, pos), 1f, Color.RED)
        }
    }

    fun selectPrefab(prefab: TilePrefab) {
        log.debug { "tile prefab '${prefab.name}' selected" }
        selection = TileSelection(prefab)
    }

    fun selectPrefab(prefab: ActorPrefab) {
        log.debug { "actor prefab '${prefab.name}' selected" }
        selection = ActorSelection(prefab)
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
        val box = Box.fromMinMax(Segment(start, end))

        return when (val selected = selection) {
            is ActorSelection -> {
                FillActorCommand(worldLogic, selected.prefab, box)
            }

            is TileSelection -> {
                FillTileCommand(worldLogic, selected.prefab, box)
            }

            else -> null
        }
    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class ActorSelection(val prefab: ActorPrefab) : Selection()
}
