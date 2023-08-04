package bke.iso.engine.world

import com.badlogic.gdx.math.Vector3
import java.util.UUID
import kotlin.properties.Delegates
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class Component

class Actor(
    val id: UUID,
    x: Float,
    y: Float,
    z: Float,
    private val onMove: (Actor) -> Unit = {}
) : GameObject() {

    var x: Float by Delegates.observable(0f) { _, _, _ ->
        onMove(this)
    }

    var y: Float by Delegates.observable(0f) { _, _, _ ->
        onMove(this)
    }

    var z: Float by Delegates.observable(0f) { _, _, _ ->
        onMove(this)
    }

    var pos: Vector3
        get() = Vector3(x, y, z)
        set(value) {
            x = value.x
            y = value.y
            z = value.z
        }

    val components = Components()

    init {
        this.x = x
        this.y = y
        this.z = z
    }

    inline fun <reified T : Component> has() =
        T::class in components

    fun move(delta: Vector3) {
        x += delta.x
        y += delta.y
        z += delta.z
    }

    override fun equals(other: Any?) =
        other is Actor && other.id == id

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString() =
        id.toString()
}

// TODO: remove this and use inline functions on a map
class Components {
    val map = mutableMapOf<KClass<out Component>, Component>()

    fun add(components: Array<out Component>) =
        components.forEach(::add)

    private inline fun <reified T : Component> add(component: T) =
        set(T::class, component)

    operator fun <T : Component> set(type: KClass<out T>, component: T) {
        map[type] = component
    }

    operator fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(map[type])

    inline fun <reified T : Component> getOrPut(defaultValue: T): T =
        map.getOrPut(T::class) { defaultValue } as T

    operator fun <T : Component> contains(type: KClass<T>) =
        map.contains(type)

    inline fun <reified T : Component> remove() =
        map.remove(T::class)
}
