package bke.iso.v2.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.math.Vector3
import java.util.UUID

class Tile(
    override val id: UUID = UUID.randomUUID(),
    location: Location,
    val sprite: Sprite,
    val solid: Boolean = false,
    override val onMove: (GameObject) -> Unit = {}
) : GameObject() {

    init {
        pos = Vector3(location.x.toFloat(), location.y.toFloat(), location.z.toFloat())
    }
}
