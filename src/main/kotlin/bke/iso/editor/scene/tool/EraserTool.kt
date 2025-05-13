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
        highlightedEntity = pickEntity()
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        val collisionBox = highlightedEntity?.getCollisionBox()
        collisionBox?.let { renderer.fgShapes.addBox(it, 1f, Color.RED) }
    }

    override fun performAction(): EditorCommand? {
        val entity = highlightedEntity
            ?: return null

        previousType = getType(entity)
        return DeleteEntityCommand(worldLogic, entity)
    }

    override fun performMultiAction(): EditorCommand? {
        val entity = highlightedEntity
            ?: return null

        val type = getType(entity)
        // avoids accidentally deleting tiles underneath an entity
        return if (type == Type.TILE && previousType == Type.TILE) {
            previousType = type
            DeleteEntityCommand(worldLogic, entity)
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
