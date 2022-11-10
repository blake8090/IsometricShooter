package bke.iso.engine.entity

import bke.iso.engine.Velocity
import com.badlogic.gdx.math.Vector2
import java.util.UUID
import kotlin.reflect.KClass

/**
 * Facade class around [Entities], providing methods for a particular entity ID.
 */
// TODO: no logic should be in this class, should just be a passthru to [Entities]
class Entity(
    val id: UUID,
    private val entities: Entities,
) {
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

    fun move(dx: Float, dy: Float): Entity {
        if (dx != 0f || dy != 0f) {
            entities.addComponent(id, Velocity(dx, dy))
        }
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

    fun <T : Component> removeComponent(type: KClass<T>) =
        entities.removeComponent(id, type)

    override fun toString(): String =
        id.toString()
}
