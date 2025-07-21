package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.asset.entity.has
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

data class FillEntityCommand(
    private val worldLogic: WorldLogic,
    private val template: EntityTemplate,
    private val box: Box
) : EditorCommand() {

    private val log = KotlinLogging.logger {}

    override val name: String = "FillEntity"

    private val replacedTiles = mutableListOf<Entity>()
    private val entities = mutableListOf<Entity>()

    override fun execute() {
        log.debug { "Filling in box: $box with template: '${template.name}'" }

        val collider = getCollider(template)
        if (collider == null) {
            log.info { "Template '${template.name}' doesn't have a collider - skipping fill" }
            return
        }

        var y = box.min.y
        while (y < box.max.y) {
            var x = box.min.x
            while (x < box.max.x) {
                create(template, x, y, box.min.z)
                x += collider.size.x
            }
            y += collider.size.y
        }
    }

    private fun getCollider(template: EntityTemplate): Collider? =
        template.components.find { component -> component is Collider }
                as? Collider

    private fun create(template: EntityTemplate, x: Float, y: Float, z: Float) {
        val pos = Vector3(x, y, z)

        if (template.has<Tile>()) {
            replaceTile(Location(pos))
        }

        entities.add(
            worldLogic.createReferenceEntity(
                template = template,
                pos = pos,
                componentOverrides = mutableSetOf()
            )
        )
    }

    private fun replaceTile(location: Location) {
        val tileEntity = worldLogic.getTileEntity(location) ?: return
        worldLogic.delete(tileEntity)
        replacedTiles.add(tileEntity)
    }

    override fun undo() {
        for (entity in entities) {
            worldLogic.delete(entity)
        }

        for (entity in replacedTiles) {
            worldLogic.add(entity)
        }
    }
}
