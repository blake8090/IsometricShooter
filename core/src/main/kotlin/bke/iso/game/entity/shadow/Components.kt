package bke.iso.game.entity.shadow

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("shadow")
data class Shadow(val parentId: String) : Component

@Serializable
@SerialName("castShadow")
class CastShadow : Component
