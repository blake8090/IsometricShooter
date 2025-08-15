package bke.iso.editor.scene

import bke.iso.editor.withFirstInstance
import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplate
import bke.iso.engine.collision.Collider
import bke.iso.engine.core.Events
import bke.iso.engine.lighting.Lighting
import bke.iso.engine.lighting.PointLight
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
import io.github.oshai.kotlinlogging.KotlinLogging

data class EntityData(
    val template: EntityTemplate,
    val componentOverrides: MutableList<Component>
)

class WorldLogic(
    private val world: World,
    private val assets: Assets,
    private val events: Events,
    private val lighting: Lighting
) {

    private val log = KotlinLogging.logger { }

    private val tilesByLocation = mutableMapOf<Location, Entity>()
    private val dataByReferenceEntity = mutableMapOf<Entity, EntityData>()
    private val deletedReferenceEntities = mutableSetOf<Pair<Entity, EntityData>>()

    fun loadScene(scene: Scene) {
        tilesByLocation.clear()
        dataByReferenceEntity.clear()
        deletedReferenceEntities.clear()
        world.clear()
        lighting.clear()

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

        sourceComponents.withFirstInstance<PointLight> { pointLight ->
            components.removeIf { c -> c::class == PointLight::class }
            components.add(pointLight.copy())
        }
    }

    fun getData(referenceEntity: Entity): EntityData =
        checkNotNull(dataByReferenceEntity[referenceEntity]) {
            "Expected EntityData for reference entity $referenceEntity"
        }

    fun delete(referenceEntity: Entity) {
        if (deletedReferenceEntities.any { (e, _) -> e == referenceEntity }) {
            log.warn { "Reference entity $referenceEntity already deleted!" }
            return
        }

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

    fun entityIsDeleted(referenceEntity: Entity): Boolean =
        deletedReferenceEntities.any { (e, _) -> referenceEntity == e }

    fun getTileEntity(location: Location): Entity? =
        tilesByLocation[location]

    fun getTileTemplateName(location: Location): String? {
        val referenceEntity = tilesByLocation[location] ?: return null

        return if (referenceEntity.has<Tile>()) {
            getData(referenceEntity).template.name
        } else {
            null
        }
    }

    /**
     * Re-adds an existing entity into the world again.
     */
    fun add(referenceEntity: Entity) {
        val (_, data) = checkNotNull(deletedReferenceEntities.firstOrNull { (entity, _) -> entity == referenceEntity }) {
            "Expected $referenceEntity to be in deletedReferenceEntities"
        }

        dataByReferenceEntity[referenceEntity] = data
        world.entities.updateGrid(referenceEntity)

        deletedReferenceEntities.remove(referenceEntity to data)

        if (referenceEntity.has<Tile>()) {
            tilesByLocation[Location(referenceEntity.pos)] = referenceEntity
        }
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
