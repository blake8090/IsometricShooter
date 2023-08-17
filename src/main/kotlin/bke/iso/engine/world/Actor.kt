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

    val components = mutableMapOf<KClass<out Component>, Component>()

    init {
        this.x = x
        this.y = y
        this.z = z
    }

    fun move(delta: Vector3) {
        x += delta.x
        y += delta.y
        z += delta.z
    }

    inline fun <reified T : Component> add(component: T) {
        components[T::class] = component
    }

    fun add(vararg components: Component) {
        for (component in components) {
            add(component)
        }
    }

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        components[T::class] as T?

    inline fun <reified T : Component> getOrPut(defaultValue: T): T =
        components.getOrPut(T::class) { defaultValue } as T

    inline fun <reified T : Component> has() =
        components.contains(T::class)

    inline fun <reified T : Component> remove() =
        components.remove(T::class)

    override fun equals(other: Any?) =
        other is Actor && other.id == id

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString() =
        id.toString()
}
