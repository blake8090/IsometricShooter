package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Box
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

data class PaintRoomCommand(
    private val worldLogic: WorldLogic,
    private val template: EntityTemplate,
    private val box: Box
) : EditorCommand() {

    override val name = "PaintRoom"

    private val log = KotlinLogging.logger {}

    private val entities = mutableListOf<Entity>()

    override fun execute() {
        log.debug { "Drawing room in box: $box with template: '${template.name}'" }

        val collider = getCollider(template)
        if (collider == null) {
            log.info { "Template '${template.name}' doesn't have a collider - skipping room creation" }
            return
        }

        var y = box.min.y
        while (y < box.max.y) {
            // draw left side
            create(
                template,
                x = box.min.x,
                y = y,
                z = box.min.z
            )

            // draw right side
            create(
                template,
                x = box.max.x - collider.size.x,
                y = y,
                z = box.min.z
            )

            y += collider.size.y
        }

        // make sure not to paint over the left and right corners
        val xMin = box.min.x + collider.size.x
        val xMax = box.max.x - collider.size.x
        var x = xMin
        while (x < xMax) {
            // draw front side
            create(
                template,
                x = x,
                y = box.min.y,
                z = box.min.z
            )

            // draw back side
            create(
                template,
                x = x,
                y = box.max.y - collider.size.y,
                z = box.min.z
            )

            x += collider.size.x
        }
    }

    private fun getCollider(template: EntityTemplate): Collider? =
        template.components.find { component -> component is Collider }
                as? Collider

    private fun create(template: EntityTemplate, x: Float, y: Float, z: Float) {
        val pos = Vector3(x, y, z)
        entities.add(worldLogic.createReferenceEntity(template, pos))
    }

    override fun undo() {
        for (entity in entities) {
            worldLogic.delete(entity)
        }
    }
}
