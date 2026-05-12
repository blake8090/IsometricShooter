package bke.iso.engine.world.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Component

@Serializable
@SerialName("description")
data class Description(val text: String = "") : Component

@Serializable
@SerialName("properties")
data class Properties(val values: MutableMap<String, String> = mutableMapOf()) : Component

@Serializable
@SerialName("tile")
class Tile : Component
