package bke.iso.editorv2.scene.tool.fill

import bke.iso.editorv2.EditorCommand
import bke.iso.editorv2.scene.ReferenceActorModule
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Box
import bke.iso.engine.world.actor.Actor
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class FillActorCommand(
    private val referenceActorModule: ReferenceActorModule,
    private val prefab: ActorPrefab,
    private val box: Box
) : EditorCommand {

    private val log = KotlinLogging.logger {}

    private val actors = mutableListOf<Actor>()

    override fun execute() {
        log.debug { "Filling in box: $box with prefab: '${prefab.name}'" }

        val collider = getCollider(prefab)
        if (collider == null) {
            log.info { "Prefab '${prefab.name}' doesn't have a collider - skipping fill" }
            return
        }

        var y = box.min.y
        while (y < box.max.y) {
            var x = box.min.x
            while (x < box.max.x) {
                create(prefab, x, y, box.min.z)
                x += collider.size.x
            }
            y += collider.size.y
        }
    }

    private fun getCollider(prefab: ActorPrefab): Collider? =
        prefab.components.find { component -> component is Collider }
                as? Collider

    private fun create(prefab: ActorPrefab, x: Float, y: Float, z: Float) {
        val pos = Vector3(x, y, z)
        actors.add(referenceActorModule.create(prefab, pos))
    }

    override fun undo() {
        for (actor in actors) {
            referenceActorModule.delete(actor)
        }
    }
}
