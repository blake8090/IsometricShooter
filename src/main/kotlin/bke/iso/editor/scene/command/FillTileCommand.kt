package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.world.entity.Entity
import io.github.oshai.kotlinlogging.KotlinLogging

data class FillTileCommand(
    private val worldLogic: WorldLogic,
    private val prefab: TilePrefab,
    private val box: Box
) : EditorCommand() {

    private val log = KotlinLogging.logger {}

    override val name: String = "FillActor"

    private val entities = mutableListOf<Entity>()

    override fun execute() {
        log.debug { "Filling in box: $box with prefab: '${prefab.name}'" }

        val xMin = box.min.x.toInt()
        val xMax = box.max.x.toInt()

        val yMin = box.min.y.toInt()
        val yMax = box.max.y.toInt()

        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                val location = Location(x, y, box.min.z.toInt())
                if (!worldLogic.tileExists(location)) {
                    entities.add(worldLogic.createReferenceActor(prefab, location))
                }
            }
        }
    }

    override fun undo() {
        for (actor in entities) {
            worldLogic.delete(actor)
        }
    }
}
