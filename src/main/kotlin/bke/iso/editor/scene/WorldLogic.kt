package bke.iso.editor.scene

import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.EntityPrefab
import bke.iso.engine.asset.prefab.TilePrefab
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Events
import bke.iso.engine.math.Location
import bke.iso.engine.render.Occlude
import bke.iso.engine.render.Sprite
import bke.iso.engine.scene.EntityRecord
import bke.iso.engine.scene.Scene
import bke.iso.engine.scene.TileRecord
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Description
import com.badlogic.gdx.math.Vector3

class WorldLogic(
    private val world: World,
    private val assets: Assets,
    private val events: Events,
) {

    private val tilesByLocation = mutableMapOf<Location, Entity>()

    fun loadScene(scene: Scene) {
        tilesByLocation.clear()
        world.clear()

        for (record in scene.actors) {
            load(record)
        }

        for (record in scene.tiles) {
            load(record)
        }

        events.fire(SceneMode.SceneLoaded())
    }

    private fun load(record: EntityRecord) {
        val prefab = assets.get<EntityPrefab>(record.prefab)
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

    fun delete(entity: Entity) {
        if (entity.has<TilePrefabReference>()) {
            tilesByLocation.remove(Location(entity.pos))
        }
        world.delete(entity)
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

    /**
     * Re-adds an existing actor into the world again.
     */
    fun add(entity: Entity) {
        world.entities.create(
            id = entity.id,
            x = entity.x,
            y = entity.y,
            z = entity.z,
            components = entity.components.values.toTypedArray()
        )
    }

    fun createReferenceActor(prefab: EntityPrefab, pos: Vector3): Entity {
        val components = mutableSetOf<Component>()
        components.add(EntityPrefabReference(prefab.name))

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

        return world.entities.create(pos, *components.toTypedArray())
    }

    fun createReferenceActor(prefab: TilePrefab, location: Location): Entity {
        if (tileExists(location)) {
            error("Duplicate tile at location $location")
        }

        val actor = world.entities.create(
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

    fun setBuilding(entity: Entity, building: String?) {
        world.buildings.remove(entity)
        if (!building.isNullOrBlank()) {
            world.buildings.add(entity, building)
        }
    }

    fun getBuilding(entity: Entity): String? =
        world.buildings.getBuilding(entity)
}
