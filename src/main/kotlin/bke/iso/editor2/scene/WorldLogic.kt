package bke.iso.editor2.scene

import bke.iso.editor.scene.ActorPrefabReference
import bke.iso.editor.scene.TilePrefabReference
import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Events
import bke.iso.engine.math.Location
import bke.iso.engine.os.Dialogs
import bke.iso.engine.render.Occlude
import bke.iso.engine.render.Sprite
import bke.iso.engine.scene.ActorRecord
import bke.iso.engine.scene.Scene
import bke.iso.engine.scene.TileRecord
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.world.World
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import bke.iso.engine.world.actor.Description
import com.badlogic.gdx.math.Vector3
import io.github.oshai.kotlinlogging.KotlinLogging

class WorldLogic(
    private val world: World,
    private val assets: Assets,
    private val events: Events,
    private val dialogs: Dialogs,
    private val serializer: Serializer,
) {

    private val log = KotlinLogging.logger {}

    private val tilesByLocation = mutableMapOf<Location, Actor>()

    fun loadScene() {
        val file = dialogs.showOpenFileDialog("Scene", "scene") ?: return
        val scene = serializer.read<Scene>(file.readText())

        tilesByLocation.clear()
        world.clear()

        for (record in scene.actors) {
            load(record)
        }

        for (record in scene.tiles) {
            load(record)
        }

        events.fire(SceneMode.SceneLoaded())
        log.info { "Loaded scene: '${file.canonicalPath}'" }
    }

    private fun load(record: ActorRecord) {
        val prefab = assets.get<ActorPrefab>(record.prefab)
        val actor = createReferenceActor(prefab, record.pos)

        for (component in record.componentOverrides) {
            actor.add(component)
        }

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(actor, building)
        }
    }

    private fun load(record: TileRecord) {
        val prefab = assets.get<TilePrefab>(record.prefab)
        val actor = createReferenceActor(prefab, record.location)

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(actor, building)
        }
    }

    fun delete(actor: Actor) {
        if (actor.has<TilePrefabReference>()) {
            tilesByLocation.remove(Location(actor.pos))
        }
        world.delete(actor)
    }

    fun deleteTile(location: Location) {
        val actor = tilesByLocation[location] ?: return
        delete(actor)
    }

    fun getTilePrefabName(location: Location): String? {
        val actor = tilesByLocation[location] ?: return null
        val reference = checkNotNull(actor.get<TilePrefabReference>()) {
            "Expected TilePrefabReference for actor $actor"
        }
        return reference.prefab
    }

    fun add(actor: Actor) {
        world.actors.create(
            id = actor.id,
            x = actor.x,
            y = actor.y,
            z = actor.z,
            components = actor.components.values.toTypedArray()
        )
    }

    fun createReferenceActor(prefab: ActorPrefab, pos: Vector3): Actor {
        val components = mutableSetOf<Component>()
        components.add(ActorPrefabReference(prefab.name))

        prefab.components.withFirstInstance<Sprite> { sprite ->
            components.add(sprite.copy())
        }

        prefab.components.withFirstInstance<Collider> { collider ->
            components.add(collider.copy())
        }

        prefab.components.withFirstInstance<Description> { description ->
            components.add(description.copy())
        }

        if (prefab.components.any { component -> component is Occlude }) {
            components.add(Occlude())
        }

        return world.actors.create(pos, *components.toTypedArray())
    }

    fun createReferenceActor(prefab: TilePrefab, location: Location): Actor {
        if (tileExists(location)) {
            error("Duplicate tile at location $location")
        }

        val actor = world.actors.create(
            location,
            prefab.sprite.copy(),
            TilePrefabReference(prefab.name),
            Collider(Vector3(1f, 1f, 0f)),
            Occlude() // manually ensure that the reference actor is included in the occlusion system
        )
        tilesByLocation[location] = actor
        return actor
    }

    fun tileExists(location: Location) =
        tilesByLocation.containsKey(location)
}
