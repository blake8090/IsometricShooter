package bke.iso.engine.entity

import com.badlogic.gdx.math.Vector2
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Facade class around [Entities], providing methods for a particular entity ID.
 */
class Entity(
    val id: UUID,
    private val entities: Entities,
) {
    fun getPos(): Vector2 =
        entities.getPos(id)
            ?: throw IllegalArgumentException("Entity id $id no longer exists!")

    fun setPos(x: Float, y: Float): Entity {
        entities.setPos(id, x, y)
        return this
    }

    fun move(dx: Float, dy: Float): Entity {
        entities.move(id, dx, dy)
        return this
    }

    fun <T : Component> addComponent(component: T): Entity {
        entities.addComponent(id, component)
        return this
    }

    fun <T : Component> getComponent(type: KClass<T>): T? =
        entities.getComponent(id, type)

    inline fun <reified T : Component> getComponent(): T? =
        getComponent(T::class)

    fun <T : Component> hasComponent(type: KClass<T>): Boolean =
        entities.hasComponent(id, type)

    inline fun <reified T : Component> hasComponent(): Boolean =
        hasComponent(T::class)
}
