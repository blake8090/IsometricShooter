package bke.iso.engine.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.render.Renderer
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.v2.Tile
import bke.iso.engine.world.v2.World
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.measureTimeMillis

class ScenesV2(
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

            for (record in scene.actors) {
                loadRecord(record)
            }

            for (record in scene.tiles) {
                loadRecord(record)
            }

            if (scene.backgroundColor != null) {
                renderer.bgColor = scene.backgroundColor
            }
        }

        log.info { "Loaded scene '$name' in $time ms" }
    }

    private fun loadRecord(record: ActorRecord) {
        val prefab = assets.get<ActorPrefab>(record.prefab)

        val components = copyComponents(prefab).toMutableList()
        for (component in record.componentOverrides) {
            components.add(component)
        }

        // TODO: configure buildings
        world.create(record.pos, *components.toTypedArray())
    }

    private fun copyComponents(prefab: ActorPrefab): Array<Component> {
        // on deserialization, we'll get completely new references
        val serialized = serializer.write(prefab.components)
        return serializer.read(serialized)
    }

    private fun loadRecord(record: TileRecord) {
        val prefab = assets.get<TilePrefab>(record.prefab)
        val components = setOf(prefab.sprite.copy(), Tile())

        // TODO: configure buildings
        world.create(record.location, *components.toTypedArray())
    }
}
