package bke.iso.engine.world.actor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Component

@Serializable
@SerialName("description")
data class Description(val text: String) : Component
