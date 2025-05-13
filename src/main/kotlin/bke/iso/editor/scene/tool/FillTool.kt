package bke.iso.editor.scene.tool

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.editor.scene.command.FillEntityCommand
import bke.iso.editor.scene.command.FillTileCommand
import bke.iso.engine.asset.prefab.EntityPrefab
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

    fun selectPrefab(prefab: EntityPrefab) {
        log.debug { "actor prefab '${prefab.name}' selected" }
        selection = EntitySelection(prefab)
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
            is EntitySelection -> {
                FillEntityCommand(worldLogic, selected.prefab, box)
            }

            is TileSelection -> {
                FillTileCommand(worldLogic, selected.prefab, box)
            }

            else -> null
        }
    }

    private sealed class Selection

    private class TileSelection(val prefab: TilePrefab) : Selection()

    private class EntitySelection(val prefab: EntityPrefab) : Selection()
}
