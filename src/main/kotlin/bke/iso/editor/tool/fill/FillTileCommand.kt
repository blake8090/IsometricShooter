package bke.iso.editor.tool.fill

import bke.iso.editor.ReferenceActors
import bke.iso.editor.tool.EditorCommand
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.math.Box
import bke.iso.engine.math.Location
import bke.iso.engine.world.actor.Actor
import io.github.oshai.kotlinlogging.KotlinLogging

class FillTileCommand(
    private val referenceActors: ReferenceActors,
    private val prefab: TilePrefab,
    private val box: Box
) : EditorCommand {

    private val log = KotlinLogging.logger {}

    private val actors = mutableListOf<Actor>()

    override fun execute() {
        log.debug { "Filling in box: $box with prefab: '${prefab.name}'" }

        val xMin = box.min.x.toInt()
        val xMax = box.max.x.toInt()

        val yMin = box.min.y.toInt()
        val yMax = box.max.y.toInt()

        for (x in xMin..xMax) {
            for (y in yMin..yMax) {
                val location = Location(x, y, box.min.z.toInt())
                if (!referenceActors.tileExists(location)) {
                    actors.add(referenceActors.create(prefab, location))
                }
            }
        }
    }

    override fun undo() {
        for (actor in actors) {
            referenceActors.delete(actor)
        }
    }
}