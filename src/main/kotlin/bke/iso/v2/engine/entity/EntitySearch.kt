package bke.iso.v2.engine.entity

import bke.iso.engine.Location
import com.badlogic.gdx.math.Rectangle
import java.util.*
import kotlin.reflect.KClass

class EntitySearch(
    private val entityById: Map<UUID, Entity>,
    private val idsByComponent: Map<KClass<out Component>, MutableSet<UUID>>,
    private val idsByLocation: Map<Location, MutableSet<UUID>>
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
        idsByLocation[Location(x, y)]
            ?.mapNotNull(entityById::get)
            ?: emptyList()

    // TODO: Finish writing out exceptions
    fun <T : Component> withComponent(type: KClass<out T>, action: (Entity, T) -> Unit) {
        val ids = idsByComponent[type] ?: return
        for (id in ids) {
            val entity = entityById[id] ?: throw IllegalArgumentException()
            val component = entity.get(type) ?: throw IllegalArgumentException()
            action.invoke(entity, component)
        }
    }

//    fun <A : Component, B : Component> withComponents(
//        typeA: KClass<A>,
//        typeB: KClass<B>,
//        action: (bke.iso.v2.engine.entity.Entity, A, B) -> Unit
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
