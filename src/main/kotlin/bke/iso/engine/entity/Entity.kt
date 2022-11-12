package bke.iso.engine.entity

import com.badlogic.gdx.math.Vector2
import java.util.*
import kotlin.reflect.KClass

/**
 * Facade class around [Entities], providing methods for a particular entity ID.
 */
class Entity(val id: UUID, private val entities: Entities) {
    fun getPos(): Vector2 =
        entities.getPos(id)

    fun setPos(x: Float, y: Float): Entity {
        entities.setPos(id, x, y)
        return this
    }

    fun setX(x: Float): Entity {
        val pos = getPos()
        setPos(x, pos.y)
        return this
    }

    fun setY(y: Float): Entity {
        val pos = getPos()
        setPos(pos.x, y)
        return this
    }

    fun <T : Component> addComponent(component: T): Entity {
        entities.components[id] = component
        return this
    }

    fun <T : Component> getComponent(type: KClass<T>): T? =
        entities.components[id, type]

    inline fun <reified T : Component> getComponent(): T? =
        getComponent(T::class)

    fun <T : Component> hasComponent(type: KClass<T>): Boolean =
        entities.components.contains(id to type)

    inline fun <reified T : Component> hasComponent(): Boolean =
        hasComponent(T::class)

    fun <T : Component> removeComponent(type: KClass<T>) =
        entities.components.remove(id, type)

    inline fun <reified T : Component> removeComponent() =
        removeComponent(T::class)

    override fun toString(): String =
        id.toString()
}
