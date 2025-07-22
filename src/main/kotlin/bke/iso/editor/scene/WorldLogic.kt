package bke.iso.editor.scene

import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
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
import bke.iso.engine.world.entity.Tile
import com.badlogic.gdx.math.Vector3

data class EntityData(
    val template: EntityTemplate,
    val componentOverrides: MutableList<Component>
)

class WorldLogic(
    private val world: World,
    private val assets: Assets,
    private val events: Events,
) {

    private val tilesByLocation = mutableMapOf<Location, Entity>()
    private val dataByReferenceEntity = mutableMapOf<Entity, EntityData>()
    private val deletedReferenceEntities = mutableSetOf<Pair<Entity, EntityData>>()

    fun loadScene(scene: Scene) {
        tilesByLocation.clear()
        dataByReferenceEntity.clear()
        deletedReferenceEntities.clear()
        world.clear()

        for (record in scene.entities) {
            load(record)
        }

        events.fire(SceneEditor.SceneLoaded())
    }

    private fun load(record: EntityRecord) {
        val template = assets.get<EntityTemplate>(record.template)
        val referenceEntity = createReferenceEntity(template, record.pos, record.componentOverrides.toMutableList())

        val building = record.building
        if (!building.isNullOrBlank()) {
            world.buildings.add(referenceEntity, building)
        }
    }

    fun createReferenceEntity(
        template: EntityTemplate,
        pos: Vector3,
        componentOverrides: MutableList<Component> = mutableListOf()
    ): Entity {
        // this is what will be used when viewing and editing components in the inspector window!
        val entityData = EntityData(template, componentOverrides)

        val referenceEntity = world.entities.create(pos)
        dataByReferenceEntity[referenceEntity] = entityData

        refreshComponents(referenceEntity)

        if (referenceEntity.has<Tile>()) {
            tilesByLocation[Location(pos)] = referenceEntity
        }

        return referenceEntity
    }

    fun createScene(): Scene {
        val entities = dataByReferenceEntity
            .map { (referenceEntity, data) ->
                EntityRecord(
                    referenceEntity.pos,
                    data.template.name,
                    getBuilding(referenceEntity),
                    data.componentOverrides
                )
            }
            .toList()

        return Scene("1", entities)
    }

    fun getReferenceEntities() =
        dataByReferenceEntity.keys

    fun refreshComponents(referenceEntity: Entity) {
        val data = getData(referenceEntity)

        val components = mutableListOf<Component>()
        add(components, data.template.components)
        add(components, data.componentOverrides)
        components.forEach(referenceEntity::add)

        // in case the Collider component changed, force an update on the grid
        // so things like collisions and entity picking still work
        world.entities.updateGrid(referenceEntity)
    }

    private fun add(components: MutableList<Component>, sourceComponents: Collection<Component>) {
        sourceComponents.withFirstInstance<Sprite> { sprite ->
            components.removeIf { c -> c::class == Sprite::class }
            components.add(sprite.copy())
        }

        sourceComponents.withFirstInstance<Collider> { collider ->
            components.removeIf { c -> c::class == Collider::class }
            components.add(collider.copy())
        }

        sourceComponents.withFirstInstance<Description> { description ->
            components.removeIf { c -> c::class == Description::class }
            components.add(description.copy())
        }

        sourceComponents.withFirstInstance<Tile> { tile ->
            components.removeIf { c -> c::class == Tile::class }
            components.add(Tile())
        }

        if (sourceComponents.any { component -> component is Occlude }) {
            components.removeIf { c -> c::class == Occlude::class }
            components.add(Occlude())
        }
    }

    fun getData(referenceEntity: Entity): EntityData =
        checkNotNull(dataByReferenceEntity[referenceEntity]) {
            "Expected EntityData for reference entity $referenceEntity"
        }

    fun delete(referenceEntity: Entity) {
        if (referenceEntity.has<Tile>()) {
            tilesByLocation.remove(Location(referenceEntity.pos))
        }
        world.delete(referenceEntity)

        val data = checkNotNull(dataByReferenceEntity[referenceEntity]) {
            "Expected EntityData for reference entity $referenceEntity"
        }
        deletedReferenceEntities.add(referenceEntity to data)
        dataByReferenceEntity.remove(referenceEntity)
    }

    fun getTileEntity(location: Location): Entity? =
        tilesByLocation[location]

    fun getTileTemplateName(location: Location): String? {
        val entity = tilesByLocation[location] ?: return null
        if (!entity.has<Tile>()) {
            return null
        }
        val reference = checkNotNull(entity.get<EntityTemplateReference>()) {
            "Expected EntityTemplateReference for entity $entity"
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

    fun setBuilding(referenceEntity: Entity, building: String?) {
        world.buildings.remove(referenceEntity)
        if (!building.isNullOrBlank()) {
            world.buildings.add(referenceEntity, building)
        }
    }

    fun getBuilding(referenceEntity: Entity): String? =
        world.buildings.getBuilding(referenceEntity)
}
