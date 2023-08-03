package bke.iso.v2.engine.world

import bke.iso.engine.entity.Component
import com.badlogic.gdx.math.Vector3
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class Actor(
    override val id: UUID = UUID.randomUUID(),
    x: Float = 0f,
    y: Float = 0f,
    z: Float = 0f,
    override val onMove: (GameObject) -> Unit = {},
) : GameObject() {

    init {
        pos = Vector3(x, y, z)
    }

    val components = Components()
}

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