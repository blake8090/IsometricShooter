package bke.iso.engine.world

import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import kotlinx.serialization.Serializable

@Serializable
data class Tile(
    val sprite: Sprite = Sprite(),
    // TODO: just use vector3?
    val location: Location = Location(),
    var selected: Boolean = false
) : GameObject {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Tile

        if (sprite != other.sprite) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        return location.hashCode()
    }
}
