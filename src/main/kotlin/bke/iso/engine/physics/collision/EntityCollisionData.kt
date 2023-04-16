package bke.iso.engine.physics.collision

data class EntityCollisionData(
    val bounds: BoundsV2,
    val box: Box,
    val solid: Boolean
)
