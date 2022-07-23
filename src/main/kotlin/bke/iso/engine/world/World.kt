package bke.iso.engine.world

import bke.iso.engine.di.Singleton
import bke.iso.engine.asset.AssetService
import bke.iso.engine.util.getLogger
import bke.iso.engine.world.asset.MapData
import bke.iso.engine.world.asset.TileTemplate
import bke.iso.engine.world.entity.Component
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.EntityDatabase
import com.badlogic.gdx.math.Vector3
import kotlin.reflect.KClass

@Singleton
class World(private val assetService: AssetService) {
    private val log = getLogger()

    val tileWidth: Int = 64
    val tileHeight: Int = 32
    val unitConverter = UnitConverter(tileWidth, tileHeight)

    private val entityDatabase = EntityDatabase()
    private val worldGrid = WorldGrid()

    fun getTile(location: Location) =
        worldGrid.getTile(location)

    fun setTile(tile: Tile, location: Location) =
        worldGrid.setTile(tile, location)

    fun createEntity() =
        Entity(
            entityDatabase.createEntity(),
            entityDatabase,
            worldGrid
        )

    fun createEntity(vararg components: Component, pos: Vector3 = Vector3()): Entity {
        val entity = createEntity()
        components.forEach(entity::setComponent)
        entity.setPosition(pos)
        return entity
    }

    fun <T : Component> findEntitiesWithComponent(componentType: KClass<out T>): Set<Entity> =
        entityDatabase.findEntitiesWithComponent(componentType)
            .map { id -> Entity(id, entityDatabase, worldGrid) }
            .toSet()

    inline fun <reified T : Component> findEntitiesWithComponent(): Set<Entity> =
        findEntitiesWithComponent(T::class)

    fun forEachLocation(action: (Location, Tile?, Set<Entity>) -> Unit) {
        worldGrid.getAll()
            .forEach { (location, data) ->
                action.invoke(
                    location,
                    data.tile,
                    data.entities
                        .map { id -> Entity(id, entityDatabase, worldGrid) }
                        .toSet()
                )
            }
    }

    fun loadMap(name: String) {
        val map = assetService.getAsset<MapData>(name)
        if (map == null) {
            log.warn("Map '$name' was not found")
            return
        }

        val templatesBySymbol = assetService.getAllAssets(TileTemplate::class)
            .associateBy { template -> template.symbol }

        worldGrid.clear()

        for ((y, columns) in map.rows.withIndex()) {
            for ((x, symbol) in columns.withIndex()) {
                templatesBySymbol[symbol]
                    ?.let(::Tile)
                    ?.let { tile ->
                        setTile(tile, Location(x, y))
                    }
                    ?: continue
            }
        }
        log.info("Successfully loaded map '$name'")
    }
}
