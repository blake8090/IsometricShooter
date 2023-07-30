package bke.iso.engine.entity

import bke.iso.engine.math.Location
import bke.iso.engine.world.WorldObject
import kotlin.reflect.KClass

class Entities(private val grid: Map<Location, MutableSet<WorldObject>>) {

    fun <T : Component> withComponent(type: KClass<out T>, action: (Entity, T) -> Unit) {
        grid.flatMap { (_, objects) -> objects }
            .filterIsInstance<Entity>()
            .filter { entity -> entity.has(type) }
            .forEach { entity ->
                val component = entity.get(type)
                    ?: throw IllegalArgumentException("Expected component ${type.simpleName}")
                action.invoke(entity, component)
            }
    }

    fun <T : Component> firstHavingComponent(type: KClass<out T>): Entity? =
        grid.flatMap { (_, objects) -> objects }
            .filterIsInstance<Entity>()
            .firstOrNull { entity -> entity.has(type) }

    inline fun <reified T : Component> firstHaving(): Entity? =
        firstHavingComponent(T::class)
}
