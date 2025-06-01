package bke.iso.engine.asset.entity

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.Serializable

@Serializable
data class EntityTemplate(
    val name: String,
    val components: MutableList<Component>
)

inline fun <reified T : Component> EntityTemplate.has(): Boolean =
    components.any { it is T }
