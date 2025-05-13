package bke.iso.game.entity.door

import bke.iso.engine.world.entity.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("door")
class Door : Component

@Serializable
@SerialName("doorOpenAction")
class DoorOpenAction : Component

@Serializable
@SerialName("doorChangeSceneAction")
data class DoorChangeSceneAction(val sceneName: String = "") : Component