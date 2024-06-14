package bke.iso.engine.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.World
import mu.KotlinLogging
import kotlin.system.measureTimeMillis

class Scenes(
    private val assets: Assets,
    private val serializer: Serializer,
    private val world: World
) {

    private val log = KotlinLogging.logger {}

    fun load(name: String) {
        val scene = assets.get<Scene>(name)

        val time = measureTimeMillis {
            world.clear()

            for (record in scene.actors) {
                load(record)
            }

            for (record in scene.tiles) {
                load(record)
            }
        }

        log.info { "Loaded scene '$name' in $time ms" }
    }

    private fun load(record: ActorRecord) {
        val prefab = assets.get<ActorPrefab>(record.prefab)
        val actor = world.actors.create(record.pos, *copyComponents(prefab))

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(actor, building)
        }
    }

    private fun copyComponents(prefab: ActorPrefab): Array<Component> {
        // on deserialization, we'll get completely new references
        val serialized = serializer.write(prefab.components)
        return serializer.read(serialized)
    }

    private fun load(record: TileRecord) {
        val prefab = assets.get<TilePrefab>(record.prefab)
        val tile = world.setTile(record.location, prefab.sprite.copy())

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(tile, building)
        }
    }
}
