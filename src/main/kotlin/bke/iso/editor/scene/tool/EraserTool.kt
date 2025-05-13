package bke.iso.editor.scene.tool

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.TilePrefabReference
import bke.iso.editor.scene.WorldLogic
import bke.iso.editor.scene.command.DeleteEntityCommand
import bke.iso.engine.collision.Collisions
import bke.iso.engine.collision.getCollisionBox
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color

class EraserTool(
    override val collisions: Collisions,
    private val worldLogic: WorldLogic,
    private val renderer: Renderer,
) : BaseTool() {

    private var previousType: Type? = null
    private var highlightedEntity: Entity? = null

    override fun update() {
        highlightedEntity = pickActor()
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        val collisionBox = highlightedEntity?.getCollisionBox()
        collisionBox?.let { renderer.fgShapes.addBox(it, 1f, Color.RED) }
    }

    override fun performAction(): EditorCommand? {
        val actor = highlightedEntity
            ?: return null

        previousType = getType(actor)
        return DeleteEntityCommand(worldLogic, actor)
    }

    override fun performMultiAction(): EditorCommand? {
        val actor = highlightedEntity
            ?: return null

        val type = getType(actor)
        // avoids accidentally deleting tiles underneath an actor
        return if (type == Type.TILE && previousType == Type.TILE) {
            previousType = type
            DeleteEntityCommand(worldLogic, actor)
        } else {
            null
        }
    }

    override fun performReleaseAction(): EditorCommand? = null

    private fun getType(entity: Entity) =
        if (entity.has<TilePrefabReference>()) {
            Type.TILE
        } else {
            Type.ENTITY
        }

    private enum class Type {
        ENTITY,
        TILE
    }
}
