package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.entity.TileTemplate
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.world.entity.Entity
import io.github.oshai.kotlinlogging.KotlinLogging

data class FillTileCommand(
    private val worldLogic: WorldLogic,
    private val template: TileTemplate,
    private val box: Box
) : EditorCommand() {

    private val log = KotlinLogging.logger {}

    override val name: String = "FillTile"

    private val entities = mutableListOf<Entity>()

    override fun execute() {
        log.debug { "Filling in box: $box with template: '${template.name}'" }

        val xMin = box.min.x.toInt()
        val xMax = box.max.x.toInt()

        val yMin = box.min.y.toInt()
        val yMax = box.max.y.toInt()

        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                val location = Location(x, y, box.min.z.toInt())
                if (!worldLogic.tileExists(location)) {
                    entities.add(worldLogic.createReferenceEntity(template, location))
                }
            }
        }
    }

    override fun undo() {
        for (entity in entities) {
            worldLogic.delete(entity)
        }
    }
}
