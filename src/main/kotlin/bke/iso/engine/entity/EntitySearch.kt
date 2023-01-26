package bke.iso.engine.entity

import bke.iso.engine.math.Location
import com.badlogic.gdx.math.Rectangle
import kotlin.reflect.KClass

class EntitySearch(
    private val entitiesByComponent: Map<KClass<out Component>, MutableSet<Entity>>,
    private val entityLocations: Map<Location, MutableSet<Entity>>
) {
    fun inArea(rect: Rectangle): Set<Entity> {
        val startX = rect.x.toInt()
        val endX = (rect.x + rect.width).toInt() + 1

        val startY = rect.y.toInt()
        val endY = (rect.y + rect.height).toInt() + 1

        val entities = mutableSetOf<Entity>()
        for (x in startX..endX) {
            for (y in startY..endY) {
                entities.addAll(atLocation(x, y))
            }
        }
        return entities
    }

    fun atLocation(x: Int, y: Int): List<Entity> =
        entityLocations[Location(x, y, 0)]
            ?.toList()
            ?: emptyList()

    // TODO: Finish writing out exceptions
    fun <T : Component> withComponent(type: KClass<out T>, action: (Entity, T) -> Unit) {
        val entities = entitiesByComponent[type] ?: return
        for (entity in entities) {
            val component = entity.get(type) ?: throw IllegalArgumentException("Expected component ${type.simpleName}")
            action.invoke(entity, component)
        }
    }

    fun <T : Component> firstHavingComponent(type: KClass<out T>): Entity? =
        entitiesByComponent[type]
            ?.firstOrNull { entity -> entity.has(type) }

//    fun <A : Component, B : Component> withComponents(
//        typeA: KClass<A>,
//        typeB: KClass<B>,
//        action: (bke.iso.engine.entity.Entity, A, B) -> Unit
//    ) {
//        val ids = components.getIdsWith(typeA)
//            .intersect(components.getIdsWith(typeB))
//        for (id in ids) {
//            val componentA = components[id, typeA]
//                ?: throw IllegalArgumentException("Expected entity id '$id' to have component '${typeA.simpleName}'")
//            val componentB = components[id, typeB]
//                ?: throw IllegalArgumentException("Expected entity id '$id' to have component '${typeB.simpleName}'")
//            action.invoke(getEntity(id), componentA, componentB)
//        }
//    }
}
