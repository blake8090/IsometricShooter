package bke.iso.engine.pathfinding

import bke.iso.engine.asset.config.Config
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PathfindingProfile(
    val name: String,
    val height: Float,
    val radius: Float,
    val maxClimb: Float,
    val maxSlope: Float
)

@Serializable
@SerialName("pathfindingConfig")
data class PathfindingConfig(
    val cellSize: Float = 0.1f,
    val cellHeight: Float = 0.05f,
    val profiles: List<PathfindingProfile>
) : Config
