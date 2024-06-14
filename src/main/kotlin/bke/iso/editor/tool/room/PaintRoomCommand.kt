package bke.iso.editor.tool.room

import bke.iso.editor.ReferenceActors
import bke.iso.editor.tool.EditorCommand
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Box
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class PaintRoomCommand(
    private val referenceActors: ReferenceActors,
    private val prefab: ActorPrefab,
    private val box: Box
) : EditorCommand {

    private val log = KotlinLogging.logger {}

    private val actors = mutableListOf<Actor>()

    override fun execute() {
        log.debug { "Drawing room in box: $box with prefab: '${prefab.name}'" }

        val collider = getCollider(prefab)
        if (collider == null) {
            log.info { "Prefab '${prefab.name}' doesn't have a collider - skipping room creation" }
            return
        }

        var y = box.min.y
        while (y < box.max.y) {
            // draw left side
            create(
                prefab,
                x = box.min.x,
                y = y,
                z = box.min.z
            )

            // draw right side
            create(
                prefab,
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
                prefab,
                x = x,
                y = box.min.y,
                z = box.min.z
            )

            // draw back side
            create(
                prefab,
                x = x,
                y = box.max.y - collider.size.y,
                z = box.min.z
            )

            x += collider.size.x
        }
    }

    private fun getCollider(prefab: ActorPrefab): Collider? =
        prefab.components.find { component -> component is Collider }
                as? Collider

    private fun create(prefab: ActorPrefab, x: Float, y: Float, z: Float) {
        val pos = Vector3(x, y, z)
        actors.add(referenceActors.create(prefab, pos))
    }

    override fun undo() {
        for (actor in actors) {
            referenceActors.delete(actor)
        }
    }
}
