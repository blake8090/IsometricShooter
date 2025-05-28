package bke.iso.editor.scene

import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.asset.entity.TileTemplate
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Events
import bke.iso.engine.math.Location
import bke.iso.engine.render.Occlude
import bke.iso.engine.render.Sprite
import bke.iso.engine.scene.EntityRecord
import bke.iso.engine.scene.Scene
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

        for (record in scene.entities) {
            load(record)
        }

        events.fire(SceneEditor.SceneLoaded())
    }

    private fun load(record: EntityRecord) {
        val template = assets.get<EntityTemplate>(record.template)
        val entity = createReferenceEntity(template, record.pos)

        for (component in record.componentOverrides) {
            entity.add(component)
        }

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(entity, building)
        }
    }

    fun delete(entity: Entity) {
        if (entity.has<TileTemplateReference>()) {
            tilesByLocation.remove(Location(entity.pos))
        }
        world.delete(entity)
    }

    fun deleteTile(location: Location) {
        val entity = tilesByLocation[location] ?: return
        delete(entity)
    }

    fun getTileTemplateName(location: Location): String? {
        val entity = tilesByLocation[location] ?: return null
        val reference = checkNotNull(entity.get<TileTemplateReference>()) {
            "Expected TileTemplateReference for entity $entity"
        }
        return reference.template
    }

    /**
     * Re-adds an existing entity into the world again.
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

    fun createReferenceEntity(template: EntityTemplate, pos: Vector3): Entity {
        val components = mutableSetOf<Component>()
        components.add(EntityTemplateReference(template.name))

        template.components.withFirstInstance<Sprite> { sprite ->
            components.add(sprite.copy())
        }

        template.components.withFirstInstance<Collider> { collider ->
            components.add(collider.copy())
        }

        template.components.withFirstInstance<Description> { description ->
            components.add(description.copy())
        }

        if (template.components.any { component -> component is Occlude }) {
            components.add(Occlude())
        }

        return world.entities.create(pos, *components.toTypedArray())
    }

    fun createReferenceEntity(template: TileTemplate, location: Location): Entity {
        if (tileExists(location)) {
            error("Duplicate tile at location $location")
        }

        val entity = world.entities.create(
            location,
            template.sprite.copy(),
            TileTemplateReference(template.name),
            Collider(Vector3(1f, 1f, 0f)),
            Occlude() // manually ensure that the reference entity is included in the occlusion system
        )
        tilesByLocation[location] = entity
        return entity
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
