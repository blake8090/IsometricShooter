package bke.iso.engine.lighting

import bke.iso.engine.core.EngineModule
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array

/**
 * Collects the point lights that the renderer can use for per-pixel lighting.
 *
 * Light positions and component values remain on their entities and are read while rendering, so movement and
 * component edits are reflected without rebuilding a tile light map.
 */
class Lighting(private val world: World) : EngineModule() {
    override val moduleName: String = "lighting"
    override val updateWhileLoading: Boolean = true
    override val profilingEnabled: Boolean = true

    var ambientLight: Color = Color.WHITE

    private val pointLightEntities = Array<Entity>(false, 16)

    override fun update(deltaTime: Float) {
        pointLightEntities.clear()
        world.entities.each { entity: Entity, _: PointLight ->
            pointLightEntities.add(entity)
        }
    }

    /** Iterates the current point lights without allocating an intermediate collection. */
    fun forEachPointLight(action: (Entity, PointLight) -> Unit) {
        for (entity in pointLightEntities) {
            val pointLight = entity.get<PointLight>() ?: continue
            action(entity, pointLight)
        }
    }

    fun clear() {
        pointLightEntities.clear()
    }
}
