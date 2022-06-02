package bke.iso.world

import bke.iso.Service
import bke.iso.asset.AssetService
import bke.iso.util.getLogger
import bke.iso.world.asset.MapData
import bke.iso.world.asset.TileTemplate
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.reflect.KClass

@Service
class World(private val assetService: AssetService) {
    private val log = getLogger(this)

    val tileWidth: Int = 64
    val halfTileWidth: Int
        get() = tileWidth / 2

    val tileHeight: Int = 32
    val halfTileHeight: Int
        get() = tileHeight / 2

    private val entityDatabase = EntityDatabase()
    private val worldGrid = WorldGrid()

    fun getTile(location: Location) =
        worldGrid.getTile(location)

    fun setTile(tile: Tile, location: Location) =
        worldGrid.setTile(tile, location)

    fun createEntity(x: Float = 0f, y: Float = 0f): Int? =
        entityDatabase.createEntity()
            ?.apply { setEntityPosition(this, x, y) }

    fun createEntity(components: List<Component>, x: Float = 0f, y: Float = 0f): Int? {
        return createEntity(x, y)
            ?.apply {
                components.forEach { component ->
                    setEntityComponent(this, component)
                }
            }
    }

    fun <T : Component> getEntityComponent(id: Int, componentType: KClass<T>): T? =
        entityDatabase.getComponent(id, componentType)

    fun <T : Component> setEntityComponent(id: Int, component: T) =
        entityDatabase.setComponent(id, component)

    fun setEntityPosition(id: Int, x: Float, y: Float) {
        if (!entityDatabase.contains(id)) {
            return
        }

        var positionComponent = entityDatabase.getComponent<PositionComponent>(id)
        if (positionComponent == null) {
            positionComponent = PositionComponent()
            entityDatabase.setComponent(id, positionComponent)
        }

        positionComponent.x = x
        positionComponent.y = y
        worldGrid.updateEntityLocation(
            id,
            Vector3(positionComponent.x, positionComponent.y, 0f)
        )
    }

    fun moveEntity(id: Int, dx: Float = 0f, dy: Float = 0f) {
        if (dx == 0f && dy == 0f) {
            return
        }
        val positionComponent = entityDatabase.getComponent<PositionComponent>(id) ?: return
        setEntityPosition(
            id,
            positionComponent.x + dx,
            positionComponent.y + dy
        )
    }

    fun forEachLocation(action: (Location, Tile?, Set<Int>) -> Unit) {
        worldGrid.getAll()
            .forEach { (location, data) ->
                action.invoke(location, data.tile, data.entities)
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

    // TODO: move these to another class
    fun locationToScreenPos(location: Location, offset: Vector2 = Vector2()): Vector2 {
        val worldPos = Vector3(
            location.x.toFloat(),
            location.y.toFloat(),
            0f
        )
        val screenPos = toScreen(worldPos)
        screenPos.add(offset)
        screenPos.x -= halfTileWidth
        screenPos.y -= tileHeight
        return screenPos
    }

    fun getEntityScreenPos(entityPos: Vector3, offset: Vector2 = Vector2()): Vector2 {
        val screenPos = toScreen(entityPos)
        screenPos.add(offset)
        return screenPos
    }

    private fun toScreen(worldPos: Vector3): Vector2 {
        // TODO: reword this comment
        // By swapping x and y and then negating them, we're now working  with vectors
        // in an isometric coordinate system where the origin is in the top left corner.
        val screenPos = Vector2(
            (worldPos.y - worldPos.x) * -1,
            (worldPos.y + worldPos.x) * -1,
        )
        screenPos.x *= halfTileWidth
        screenPos.y *= halfTileHeight
        return screenPos
    }
}
