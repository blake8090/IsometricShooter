package bke.iso.engine.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.render.Renderer
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.World
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.measureTimeMillis

class Scenes(
    private val assets: Assets,
    private val serializer: Serializer,
    private val world: World,
    private val renderer: Renderer
) {

    private val log = KotlinLogging.logger {}

    fun load(name: String) {
        val scene = assets.get<Scene>(name)

        val time = measureTimeMillis {
            world.clear()

            for (record in scene.entities) {
                load(record)
            }

            if (scene.backgroundColor != null) {
                renderer.bgColor = scene.backgroundColor
            }
        }

        log.info { "Loaded scene '$name' in $time ms" }
    }

    private fun load(record: EntityRecord) {
        val template = assets.get<EntityTemplate>(record.template)

        val components = copyComponents(template).toMutableList()
        for (component in record.componentOverrides) {
            components.add(component)
        }

        val entity = world.entities.create(record.pos, *components.toTypedArray())

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(entity, building)
        }
    }

    private fun copyComponents(template: EntityTemplate): Array<Component> {
        // on deserialization, we'll get completely new references
        val serialized = serializer.write(template.components)
        return serializer.read(serialized)
    }
}
