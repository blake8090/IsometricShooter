package bke.iso.world.entity

import bke.iso.world.WorldGrid
import com.badlogic.gdx.math.Vector3
import kotlin.reflect.KClass

class Entity(
    private val id: Int,
    private val entityDatabase: EntityDatabase,
    private val worldGrid: WorldGrid
) {
    fun <T : Component> getComponent(componentType: KClass<T>): T? =
        entityDatabase.getComponent(id, componentType)

    inline fun <reified T : Component> getComponent() =
        getComponent(T::class)

    fun <T : Component> setComponent(component: T) =
        entityDatabase.setComponent(id, component)

    fun setPosition(x: Float, y: Float) {
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

    fun setPosition(pos: Vector3) =
        setPosition(pos.x, pos.y)

    fun move(dx: Float = 0f, dy: Float = 0f) {
        if (dx == 0f && dy == 0f) {
            return
        }
        val positionComponent = entityDatabase.getComponent<PositionComponent>(id) ?: return
        setPosition(
            positionComponent.x + dx,
            positionComponent.y + dy
        )
    }
}
