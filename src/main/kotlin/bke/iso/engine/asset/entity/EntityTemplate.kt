package bke.iso.engine.asset.entity

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data class EntityTemplate(
    val name: String,
    val components: List<Component>
)

fun <T : Component> EntityTemplate.has(type: KClass<T>): Boolean =
    components.any { it::class == type }

inline fun <reified T : Component> EntityTemplate.has(): Boolean =
    has(T::class)
