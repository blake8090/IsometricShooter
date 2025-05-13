package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.editor.scene.WorldLogic
import bke.iso.engine.asset.prefab.EntityPrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.math.Box
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

data class FillEntityCommand(
    private val worldLogic: WorldLogic,
    private val prefab: EntityPrefab,
    private val box: Box
) : EditorCommand() {

    private val log = KotlinLogging.logger {}

    override val name: String = "FillEntity"

    private val entities = mutableListOf<Entity>()

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

    private fun getCollider(prefab: EntityPrefab): Collider? =
        prefab.components.find { component -> component is Collider }
                as? Collider

    private fun create(prefab: EntityPrefab, x: Float, y: Float, z: Float) {
        val pos = Vector3(x, y, z)
        entities.add(worldLogic.createReferenceEntity(prefab, pos))
    }

    override fun undo() {
        for (entity in entities) {
            worldLogic.delete(entity)
        }
    }
}
