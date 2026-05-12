package bke.iso.editor.scene.tool

import bke.iso.editor.core.command.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.editor.scene.command.DeleteEntityCommand
import bke.iso.engine.collision.CollisionBoxes
import bke.iso.engine.collision.Collisions
import bke.iso.engine.render.Renderer
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.graphics.Color

class EraserTool(
    override val collisions: Collisions,
    private val worldLogic: WorldLogic,
    private val renderer: Renderer,
    private val collisionBoxes: CollisionBoxes
) : BaseTool() {

    private var previousType: Type? = null
    private var highlightedEntity: Entity? = null

    override fun update() {
        highlightedEntity = pickEntity()
    }

    override fun draw() {
        renderer.fgShapes.addPoint(pointerPos, 1f, Color.RED)

        highlightedEntity?.let(this::drawCollisionBox)
    }

    private fun drawCollisionBox(entity: Entity) {
        collisionBoxes[entity]?.let { box ->
            renderer.fgShapes.addBox(box, 1f, Color.RED)
        }
    }

    override fun performAction(): EditorCommand? {
        val entity = highlightedEntity
            ?: return null

        if (worldLogic.entityIsDeleted(entity)) {
            return null
        }

        previousType = getType(entity)
        return DeleteEntityCommand(worldLogic, entity)
    }

    override fun performMultiAction(): EditorCommand? {
        val entity = highlightedEntity
            ?: return null

        if (worldLogic.entityIsDeleted(entity)) {
            return null
        }

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
        if (entity.has<Tile>()) {
            Type.TILE
        } else {
            Type.ENTITY
        }

    private enum class Type {
        ENTITY,
        TILE
    }
}
