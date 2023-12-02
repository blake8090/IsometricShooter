package bke.iso.engine.scene

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.Component
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
                val prefab = assets.get<ActorPrefab>(record.prefab)
                world.actors.create(record.pos, *copyComponents(prefab))
            }

            for (record in scene.tiles) {
                val prefab = assets.get<TilePrefab>(record.prefab)
                world.setTile(record.location, prefab.sprite.copy())
            }
        }

        log.info { "Loaded scene '$name' in $time ms" }
    }

    private fun copyComponents(prefab: ActorPrefab): Array<Component> {
        // on deserialization, we'll get completely new references
        val serialized = serializer.write(prefab.components)
        return serializer.read(serialized)
    }
}
