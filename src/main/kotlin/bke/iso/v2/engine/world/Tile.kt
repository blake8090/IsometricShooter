package bke.iso.v2.engine.world

import bke.iso.engine.math.Location
import java.util.UUID

data class Tile(
    override val id: UUID = UUID.randomUUID(),
    override var x: Float = 0f,
    override var y: Float = 0f,
    override var z: Float = 0f,
    val texture: String = "",
    val solid: Boolean = false,
    override val onMove: (GameObject) -> Unit = {}
) : GameObject() {
    constructor(
        id: UUID = UUID.randomUUID(),
        location: Location = Location(),
        texture: String = "",
        solid: Boolean = false,
        onMove: (GameObject) -> Unit = {}
    ) : this(
        id,
        location.x.toFloat(),
        location.y.toFloat(),
        location.z.toFloat(),
        texture,
        solid,
        onMove
    )
}
