package bke.iso.v2.engine.world

import bke.iso.engine.math.Location
import com.badlogic.gdx.math.Vector3
import java.util.UUID

class Tile(
    override val id: UUID = UUID.randomUUID(),
    x: Float = 0f,
    y: Float = 0f,
    z: Float = 0f,
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

    init {
        pos = Vector3(x, y, z)
    }
}
