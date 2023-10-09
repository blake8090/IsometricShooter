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
) : GameObject
